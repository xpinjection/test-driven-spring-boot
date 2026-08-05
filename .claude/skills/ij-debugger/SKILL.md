---
name: ij-debugger
description: "Debugger-first runtime root-cause analysis for JVM code in IntelliJ IDEA. Use when runtime state or control-flow evidence is needed (values, branches, call order, reachability), when the question spans long call chains across many files, or when the user explicitly requests the debugger. Evidence comes from logpoints/breakpoints, stepping, stacks, runtime values, expression evaluation, and control-flow observations."
allowed-tools: execute_tool
---

# IntelliJ IDEA Debugger Skill

All debugger and run-configuration tools are invoked through the universal router:
`execute_tool(command="<tool> --arg value ...")`. Do not call the underlying handles directly.

Router argument rules:
- Every `--param` must be followed by a value; bare flags are not supported.
- Boolean params need an explicit `true`/`false` (e.g. `--isLogStack true`).
- **Quote every value that contains a space or an operator — expressions almost always do.** The router splits `command` on whitespace, so an unquoted multi-token value is misread as extra arguments and the call fails with an error like `Invalid argument format: '+'. Expected '--paramName value' format`. Wrap the whole value in single quotes so it stays one token:
  - `--logExpression 'a.getCount() + 1'` (not `--logExpression a.getCount() + 1`)
  - `--condition 'id == 42 && name != null'`
  - `--expression 'user.getName().trim()'`
  - If the expression itself contains single quotes (e.g. a `char`/`String` literal), wrap the outer value in double quotes instead: `--logExpression "name == 'tax_rate'"`. Do not escape quotes with backslashes and do not JSON-encode the value.
- Omit optional params entirely instead of passing `""`, `"/"`, `"."`, `"__omit__"`, or other sentinels.

## Goal
Use debugger evidence to answer concrete runtime questions when static code reading is not enough, or to confirm ideas from static analysis — including whether execution reaches or does not reach agent-selected code locations. Gather this evidence with **minimal runtime disturbance**: by default observe without stopping threads and without editing source code.

## When To Use This Skill
Use this skill when at least one holds:
- a runtime value or control-flow fact is needed (actual values, branch taken, call order, thread context, or whether execution reaches a chosen location) and it cannot be derived confidently from source/logs;
- finding the relevant path would otherwise require reading many files or tracing a long call chain manually;
- the user explicitly asks to use the debugger (breakpoints, logpoints, stepping, inspect values, debugger session).

Manual activation by the user means launch and use the debugger when tools are available — do not reduce it to a conceptual discussion. Do not treat domain identifiers named `debug`/`debugger` in the user's own code as a debugger request; reason about what the user actually means.

## Activation Gate (tool availability)
Use this skill only when the debugger router tools are available. Minimum required set (each invoked via `execute_tool`):
- `xdebug_set_breakpoint` (important: sets logpoints, via `--logExpression`)
- `xdebug_start_debugger_session`
- `xdebug_control_session`
- `xdebug_get_stack`
- at least one value tool: `xdebug_get_frame_values`, `xdebug_get_value_by_path`, or `xdebug_evaluate_expression`
- `get_run_configurations`

If the minimum set is unavailable: state the blocker explicitly, do not force a debugger workflow, and switch to the best fallback. Run `execute_tool(command="<tool> --help")` to inspect a tool's exact current contract.

## Principle: Logpoint-First Debugging
**Logpoints are the primary way to communicate with the IntelliJ debugger.** A logpoint is a non-suspending breakpoint that does not stop execution: when the line is reached it evaluates an expression and logs the result. Reach for a logpoint first for almost every runtime question; treat suspending breakpoints, stepping, and expression evaluation as escalations you must justify, not as the default. Logpoints give runtime evidence without freezing threads — which matters for concurrent code, long-running services, flaky/timing-sensitive tests, and multi-agent workflows.

**Logpoint output is dumped to debugger events, and you read it back from there.** Each logpoint hit is captured as an event in the debug session's event buffer — not printed to your console and not written to any file. You collect that output by draining events: `execute_tool(command="xdebug_control_session --action DRAIN_EVENTS")` returns the accumulated logpoint output in `tracepointOutputsTail`. So the logpoint loop is: install logpoint(s) → run the scenario → drain events → map each event back to source. Reading events is the normal, expected way to get logpoint results; if you find yourself wanting to write a file or add a print statement to capture output, drain events instead.

Prefer the **least disruptive** tool that answers the question, in this priority order:

1. **Non-suspending logpoints** — `execute_tool(command="xdebug_set_breakpoint --filePath <p> --line <n> --logExpression '<expr>' --suspendPolicy NONE")`. A logpoint is just `xdebug_set_breakpoint` with a `--logExpression`; add `--suspendPolicy NONE` so it does not stop execution. Providing the `--logExpression` is the most important thing you do.
   Use for runtime values, branch evidence, counts, identifiers, and path/reachability confirmation. This is the default first probe.
2. **Logpoints with condition / stack-trace logging** — add `--condition '<expr>'` to filter, and `--isLogStack true` for a call path.
   Use when events are too frequent, only specific cases matter, or a call path is needed.
3. **Thread / stack inspection of a suspended session** — `execute_tool(command="xdebug_get_threads")` + `execute_tool(command="xdebug_get_stack ...")`.
   These read a *paused* session: if the program is running, suspend it first with `execute_tool(command="xdebug_control_session --action PAUSE")`, then read threads/stack. Use for hangs, concurrency issues, deadlocks, thread-state questions, or unclear call paths.
4. **Ordinary suspending breakpoints** — `execute_tool(command="xdebug_set_breakpoint --filePath <p> --line <n>")` (the same tool without `--logExpression`, keeping the default `--suspendPolicy ALL`).
   Use only when you must inspect live object graphs, evaluate multiple expressions interactively, step through code, or modify state.
5. **Evaluate expression / Set value** — `execute_tool(command="xdebug_evaluate_expression ...")`, `execute_tool(command="xdebug_set_variable ...")`.
   Use only in a suspended context and only with explicit justification; these can alter what you are investigating.

Escalate down this list only when the cheaper option cannot answer the question, and say why you escalated.

## Safe Logpoint Expressions (no side effects)
`--logExpression` and `--condition` are evaluated inside the running program and **can change its behavior**. By default restrict them to **side-effect-free reads**: field/local/parameter access, non-mutating getters, arithmetic, `toString()` on trusted values, identity/`hashCode` checks.

Do **not** use expressions that mutate state, perform I/O, advance iterators/streams, trigger side-effecting lazy initialization, or call methods with observable side effects — unless the user explicitly asked to enter a controlled mutation/debugging mode and accepted that program behavior may change.

## Logpoint Placement: Variable Scope And Initialization
A logpoint evaluates its `--logExpression` **at the moment execution reaches the line, before that line's statement runs**. Every symbol the expression references must already be **assigned and in scope** at that point, or evaluation fails (you will see an error such as `Variable 'x' has not been initialized` / `Cannot find local variable 'x'` in `breakpointErrorsTail`, and no value is logged).

Rules for picking the line:
- Do **not** place a logpoint on the line that *declares or initializes* the variable you want to log, nor in the middle of a multi-line statement that assigns it. At that position the variable does not hold its value yet.
- For a multi-line assignment such as a fluent/builder chain
  ```
  List<String> configured = jdbc.sql("…")   // line 20  ← declaration starts here
      .query(String.class)                   // line 21
      .list();                               // line 22  ← assignment completes here
  this.rate = new BigDecimal(configured.get(0)); // line 23
  ```
  a logpoint on line 21 logging `configured.get(0)` fails — `configured` is assigned only after line 22. Place it on the **first line after the statement's terminating `;`** (here line 23), where `configured` is initialized.
- General rule: put the logpoint on the earliest executable line where every symbol in the expression is already initialized and in scope. To capture a value at a method's entry, target the first executable line of the body, not the signature/parameter line.
- After installing, run once and read `breakpointErrorsTail`. If you see an uninitialized/unresolved-variable error, move the logpoint to a later line (or pick an expression whose inputs are already available) and rerun.

## Initial Triage Before Debugging
Start with minimal static triage (error text, stack trace, nearby source). Start debugger work when: static analysis leaves multiple plausible runtime hypotheses; deciding between them requires concrete values or exact control flow; stepping is cheaper than reading a large cross-file path; or the user explicitly asked. Install the first logpoint(s) early and collect evidence before changing runtime behavior.

## Mandatory Runtime Evidence
Before editing code for a runtime-behavior issue when debugger evidence is available, capture:
- for reachability/branch questions: logpoint output proving execution did or did not hit a chosen location (or its absence across a full reproduction);
- for state questions: at least one concrete runtime value, via a logpoint expression or — in a suspended session — `xdebug_get_frame_values` / `xdebug_get_value_by_path` / `xdebug_evaluate_expression`;
- when you suspend: the paused location (file/line) and the top call path via `xdebug_get_stack`.

Do not assert runtime conclusions before this evidence is captured unless a clear blocker is already stated.

## Hypothesis-Driven Batch Logpoints
Logpoints are cheap and non-suspending, so prefer installing a **batch** derived from your hypotheses, then run the scenario once:
1. Form 2–5 concrete hypotheses about where/why runtime behavior diverges.
2. Install one logpoint per hypothesis anchor (branch decision, mapping boundary, state transition, dynamic dispatch, error construction, queue handoff), each logging the values that confirm or refute that hypothesis.
3. Run the scenario once via `execute_tool(command="xdebug_start_debugger_session ...")`.
4. Read output with `execute_tool(command="xdebug_control_session --action DRAIN_EVENTS")` and map each line back to its source location and hypothesis.
5. Refine hypotheses or escalate to stronger tools only when logpoint evidence is insufficient.

## Reproduction Mode Selection
1. `AUTO`: you can run/rerun the scenario directly. Use `xdebug_start_debugger_session` to launch with debugging, or `execute_run_configuration` for a non-debug run when you only need output/exit code.
2. `ASSISTED`: reproduction needs a user-only action (UI flow, auth, external dependency, hardware).
3. `HYBRID`: try `AUTO` once or twice, then switch to `ASSISTED` if it does not reproduce.

If the candidate target is a test, default to `AUTO`. Never ask the user to reproduce before logpoints/breakpoints are prepared.

## Test Task Execution
- To debug a test: `execute_tool(command="xdebug_start_debugger_session --filePath <p> --line <n>")` pointing at the test method, or `--configurationName <name>` for an existing test run configuration. To run without debugging (e.g. confirm it fails first): `execute_tool(command="execute_run_configuration ...")` with the same targeting modes.
- After triggering, inspect state with `execute_tool(command="xdebug_get_debugger_status")` and continue inside the resulting session.
- Do not ask the user to rerun a test manually unless no available path can reproduce it or repro requires user-only setup.

## Argument Hygiene
- Optional params are omitted or set to a real value; omitted means omitted.
- Never encode omitted params with placeholder strings such as `""`, `"/"`, `"__omit__"`, `"."`, or fake paths.
- Treat debugger IDs and paths as opaque runtime data: copy them from tool outputs, do not synthesize them.

## Session Binding
- Before preparing logpoints/breakpoints or launching, call `execute_tool(command="xdebug_get_debugger_status")`.
- If an active relevant session exists, continue inside it instead of starting another.
- Capture the exact `sessionId` from status/start and reuse it in all session-scoped calls (`--sessionId <id>`).
- If a session stops, times out, or disappears, do not reuse the old `sessionId`; refresh via `xdebug_get_debugger_status` first. After the paused location changes, re-read `frameIndex`/`path` from a fresh `xdebug_get_stack`.

## Run Configuration Resolution
- Resolve `configurationName` only from `execute_tool(command="get_run_configurations")`; do not pass a test method name or other derived identifier.
- Use `supportsDynamicLaunchOverrides` from `get_run_configurations` as the source of truth for `--programArguments`, `--workingDirectory`, and `--envs`.
- Use launch overrides only when they materially improve reproducibility/observability and the run config supports them.

## Breakpoint And Logpoint Ownership And Hygiene
- Start with `execute_tool(command="xdebug_list_breakpoints")` and treat returned `owner` as source of truth (`user`/`agent`). Logpoints appear here too.
- Snapshot the **full** baseline of each breakpoint you may touch — `enabled`, `logExpression`, `condition`, `isLogMessage`, `isLogStack`, `suspendPolicy`, and `temporary` (all returned by `xdebug_list_breakpoints`). You need this because a `breakpointId`-mode update rewrites the whole breakpoint (see Targeting Modes).
- Avoid broad breakpoint churn between iterations — change the minimum set needed for the next probe.
- `xdebug_remove_breakpoint` defaults to `--owner agent`; global cleanup needs two calls: `--owner agent` then `--owner user`.

## Breakpoint And Logpoint Targeting Modes
`xdebug_set_breakpoint` (whether or not you pass `--logExpression`) has two mutually exclusive modes — never mix in one call:
- **Location mode**: pass `--filePath` + `--line` (1-based); omit `--breakpointId`.
- **`breakpointId` mode**: pass `--breakpointId <id>` (an opaque canonical id from a prior `xdebug_set_*`/`xdebug_list_breakpoints`).

`breakpointId` mode rewrites the whole breakpoint, it does not patch it. Treat an update as "write the full breakpoint", never "tweak one flag".

After each call, inspect the returned `lineText` and confirm the excerpt matches the intended line. A successful response does not prove `--condition`/`--logExpression` are valid; verify via `breakpointErrorsTail` / `tracepointOutputsTail` after the next run.

## Core Workflow
1. Scope the failure: capture exact error text and expected vs actual; identify 2–5 candidate anchors and the value at each that would confirm/refute a hypothesis.
2. Prepare: `execute_tool(command="xdebug_get_debugger_status")` → reuse a relevant session or plan a new one; pick `AUTO`/`ASSISTED`/`HYBRID`; install logpoints first and verify each `lineText`.
3. Run: reuse an active session, or `execute_tool(command="xdebug_start_debugger_session ...")` and let the scenario run (logpoints do not suspend).
4. Collect: `execute_tool(command="xdebug_control_session --action DRAIN_EVENTS")`; map each output line to source + hypothesis.
5. Escalate only if needed: if a logpoint cannot capture the needed object graph or interactive evidence, add a suspending breakpoint at the proven-relevant line, then `--action WAIT_FOR_PAUSE` and inspect stack + values.
6. After every `--action RESUME` of a suspended session, always `execute_tool(command="xdebug_control_session --action WAIT_FOR_PAUSE")`.
7. On wait timeout: `--action PAUSE`, re-check enabled breakpoints and the expected path; after 2 consecutive timeouts on the same wait, stop retrying and expand logpoint coverage.
8. Continue until the first incorrect state transition is proven or you have a clearly stated blocker; then summarize root cause with concrete evidence.

## Default First Probe
For a reproducible runtime issue:
1. `execute_tool(command="xdebug_get_debugger_status")`
2. Reuse an active relevant session, otherwise install a logpoint: `execute_tool(command="xdebug_set_breakpoint --filePath <p> --line <n> --logExpression '<expr>' --suspendPolicy NONE")`
3. If no relevant session exists, `execute_tool(command="xdebug_start_debugger_session ...")`
4. `execute_tool(command="xdebug_control_session --action DRAIN_EVENTS")` to read logpoint output
5. Map output to source; escalate to a suspending breakpoint + `xdebug_get_stack` / `xdebug_get_frame_values` only if logpoints are insufficient

## Tool Reference
Invoke each via `execute_tool(command="<tool> ...")`. The router schema (`execute_tool(command="<tool> --help")`) is the source of truth for exact parameters and constraints.

- `xdebug_set_breakpoint [--filePath <p> --line <n>] [--breakpointId <id>] [--logExpression '<expr>'] [--condition '<expr>'] [--isLogMessage true|false] [--isLogStack true|false] [--temporary true|false] [--suspendPolicy ALL|THREAD|NONE] [--enabled true|false]`:
  the single tool for breakpoints and logpoints. **To set a logpoint (the preferred default probe), pass `--logExpression '<expr>' --suspendPolicy NONE`** — it evaluates the expression and logs the result on every hit without suspending; read output via `xdebug_control_session --action DRAIN_EVENTS`. Keep `--logExpression` side-effect-free. Without `--logExpression` it is an ordinary suspending breakpoint; `--isLogMessage`/`--isLogStack` (+ `--suspendPolicy NONE`) make a position/stack tracepoint. Providing `--logExpression` is the most important input. In `--breakpointId` mode this call rewrites the whole breakpoint, so re-pass its complete state (omitted fields are reset — see Targeting Modes).
- `xdebug_start_debugger_session [--configurationName <name>] | [--filePath <p> --line <n>] [--timeout <ms>] [--graceWaitMs <ms>] [--programArguments <s>] [--workingDirectory <p>] [--envs <map>]`:
  start debugging a run configuration or a code location. Use exactly one target mode.
- `xdebug_get_debugger_status`: list sessions/states; bind the current `sessionId` and refresh it after `stopped`/disappearance.
- `xdebug_control_session --action <STEP_INTO|STEP_OVER|STEP_OUT|RESUME|PAUSE|STOP|WAIT_FOR_PAUSE|DRAIN_EVENTS> [--sessionId <id>] [--timeout <ms>] [--eventsLimit <n>]`:
  control the session; `DRAIN_EVENTS` returns logpoint/tracepoint output in `tracepointOutputsTail`. A `paused` result includes `frameValues`.
- `xdebug_get_threads [--sessionId <id>] [--limit <n>] [--offset <n>]`: list threads (suspended session).
- `xdebug_get_stack [--sessionId <id>] [--threadId <id>] [--limit <n>] [--offset <n>]`: call stack; `frameIndex` consumers use the current paused result only.
- `xdebug_get_frame_values [--sessionId <id>] [--frameIndex <n>] --depth <n>`: locals/values in a frame.
- `xdebug_get_value_by_path [--sessionId <id>] [--frameIndex <n>] --path <list> --depth <n>`: drill into nested values.
- `xdebug_evaluate_expression [--sessionId <id>] [--frameIndex <n>] --expression '<expr>' --depth <n>`: evaluate in the current frame (suspended session).
- `xdebug_remove_breakpoint [--breakpointId <id>] [--filePath <p> --line <n>] [--owner agent|user]`: remove breakpoints/logpoints filtered by owner.
- `xdebug_list_breakpoints [--filePath <p>]`: list breakpoints and logpoints with ownership.
- `xdebug_run_to_line [--sessionId <id>] --filePath <p> --line <n> [--timeout <ms>]`: continue to a line (suspended session).
- `xdebug_set_variable [--sessionId <id>] [--frameIndex <n>] --path <list> --newValue '<expr>'`: mutate a value (explicit justification only).
- `get_run_configurations [--filePath <p>]`: list run configurations (with `supportsDynamicLaunchOverrides`), or discover runnable entry points/line numbers in a file.
- `execute_run_configuration [--configurationName <name>] | [--filePath <p> --line <n>] [--timeout <ms>] [--waitForExit true|false] [--programArguments <s>] [--workingDirectory <p>] [--envs <map>]`: run without debugging.

## Events, Logpoints, And Tracepoints
Logpoint and tracepoint output is buffered as **debugger events** on the session; you consume it by draining, not by reading a console or file.

- **Read logpoint output:** `execute_tool(command="xdebug_control_session --action DRAIN_EVENTS")` returns the buffered output in `tracepointOutputsTail` (`xdebug_set_breakpoint --logExpression`, and `--isLogMessage`/`--isLogStack`).
- **Each event maps back to source.** A drained event carries the evaluated `message`, plus `breakpointId`, `filePath`, `line`, and a timestamp — use these to attribute every line to the logpoint and hypothesis that produced it. Distinguish logpoints by their location/expression so concurrent or interleaved hits stay separable.
- **Drain is consuming:** events are removed from the buffer when drained, so each call returns only output produced since the last drain. Drain after the scenario finishes; for long-running or high-volume scenarios, drain periodically during the run so the bounded buffer does not overflow and drop the oldest events. Use `--eventsLimit <n>` to cap how many are returned per call.
- **Errors come back as events too:** every `xdebug_control_session` response carries `breakpointErrorsTail` with breakpoint/logpoint validation or runtime errors (e.g. a bad `--logExpression`). After installing any logpoint/condition/tracepoint, do not trust it until you have drained and read both tails.
- Event tails are currently populated by JVM-based debuggers (Java, Kotlin, etc.).

## Expression Discipline
- `--logExpression` and `--condition` run in the program; keep them side-effect-free (see Safe Logpoint Expressions).
- `--expression` (`xdebug_evaluate_expression`) runs in the current paused frame; pass raw expression text in that frame's language.
- `--newValue` (`xdebug_set_variable`) must be a raw expression assignable to the target value.
- Expression values nearly always contain spaces or operators (`+`, `==`, `&&`, method args), so they MUST be quoted as a single token (see Router argument rules). An unquoted expression fails with `Invalid argument format: '<token>'`. Do not pass JSON-escaped or backslash-escaped quoted text into expression params. The router parses the outer `command` with a shell-like splitter: when the expression contains single quotes wrap the outer string in double quotes, and vice versa.
- Prefer fully-qualified names for global/static symbols in `--condition`, `--logExpression`, and `--expression` to avoid unresolved-reference errors from missing imports.
- If paused, preflight a risky condition/log expression with `xdebug_evaluate_expression` before relying on it.

## Anti-Patterns
- adding temporary `print`/log statements to source (or trying to capture logpoint output to a file) instead of installing a logpoint and draining its events;
- concluding from a logpoint without draining events (`DRAIN_EVENTS`), or assuming a re-drain will return output already consumed by an earlier drain;
- defaulting to suspending breakpoints when a non-suspending logpoint would answer the question;
- using side-effecting log/condition expressions without explicit user consent;
- placing a logpoint on the line that initializes the variable it logs (or inside a multi-line assignment), so the variable is not yet initialized when the expression runs;
- claiming root cause without runtime values;
- calling debugger handles directly instead of through `execute_tool`;
- encoding omitted params with placeholder strings;
- reusing stale `sessionId`/`frameIndex`/`path` after the paused location changes;
- mixing location mode and `breakpointId` mode in one call;
- updating a breakpoint by id without re-passing its full state, clobbering a user's `logExpression`/`condition` or resetting its log/suspend flags;
- stopping at a downstream symptom without tracing producing state;
- asking the user to reproduce before probes are prepared;
- `RESUME` without a justified expected next stop;
- prolonged static-only analysis when one logpoint probe can disambiguate;
- ignoring library frames instead of reading their decompiled source; extracting JARs manually instead of using `read_file`.

## Wrap-up
After debugging:
1. Clean up: `execute_tool(command="xdebug_remove_breakpoint --owner agent")`, restore disabled user breakpoints to baseline, and `execute_tool(command="xdebug_control_session --action STOP")` if the session is no longer needed.
2. Report: root cause in one sentence; causal chain in 3–6 bullets; runtime evidence with exact observed values; code references (absolute path + line); if you fell back instead of debugging, the exact reason and what evidence is still missing.
3. Next action: if the user asked for diagnosis only, conclude with the report and a recommended fix; if the task implies a code change, implement the fix based on the proven root cause and collected evidence.
