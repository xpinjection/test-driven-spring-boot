#!/usr/bin/env bash
# PreToolUse hook: block edits to ALREADY-EXISTING Flyway migrations.
#
# Why: Flyway computes a checksum for every applied migration. Editing a
# migration that has already run breaks application startup in every
# environment. So we deny Write/Edit on migrations that are already committed
# to git (present in HEAD), while still allowing free iteration on NEW
# migrations created during the current task (untracked or merely staged ->
# not in HEAD).
#
# Output contract: print a PreToolUse "deny" JSON on stdout to block the call;
# print nothing (exit 0) to let it proceed.

# 1. Read the hook payload from stdin and pull out the target file path.
INPUT=$(cat)
FILE=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')

# 2. Only act on Flyway migration files: V<version>__<name>.sql.
#    Anything else: allow, nothing to do.
case "$FILE" in
  *db/migration/V*__*.sql) ;;   # matched -> continue to the check below
  *) exit 0 ;;
esac

# 3. Reconstruct the repo-relative path. Migrations live flat in one directory,
#    so the basename is enough to address the file inside git.
REL="src/main/resources/db/migration/$(basename "$FILE")"

# 4. Is this migration already committed (exists in HEAD)?
#      exit 0   -> committed  -> "existing" migration -> must stay immutable
#      exit !=0 -> not in HEAD -> new in this task    -> allow the edit
if git cat-file -e "HEAD:$REL" 2>/dev/null; then
  cat <<'JSON'
{"hookSpecificOutput":{"hookEventName":"PreToolUse","permissionDecision":"deny","permissionDecisionReason":"This Flyway migration is already committed (exists in git HEAD) and is immutable — changing an applied migration breaks Flyway checksums. Create a new V1.XX__ migration file instead."}}
JSON
fi

# 5. No output above -> allow the tool call.
exit 0
