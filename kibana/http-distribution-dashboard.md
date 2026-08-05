# HTTP Request Status Distribution Dashboard — Step-by-Step Guide

This guide walks through creating a Kibana dashboard that monitors the distribution of HTTP requests
by response status code, using structured logs produced by the Spring Boot Library application.

## Prerequisites

- Spring Boot application running on port 8080 with profiles `dev` and `json-logs` active
- Elasticsearch running on port 9200
- Kibana running on port 5601
- OpenTelemetry Collector shipping logs from `logs/library-logs.json` to Elasticsearch
- At least a few HTTP requests made to the application so log data exists

### Verify data is flowing

Before starting, confirm logs have reached Elasticsearch:

```bash
curl "http://localhost:9200/.ds-logs-generic-default-*/_search" \
  -H "Content-Type: application/json" \
  -d '{
    "size": 1,
    "query": { "match": { "logger_name": "org.zalando.logbook.Logbook" } }
  }'
```

The response should contain at least one hit. If it returns zero hits, generate some traffic first:

```bash
# Generate 200 responses
curl "http://localhost:8080/books?author=Fowler"

# Generate 404 responses
curl "http://localhost:8080/nonexistent"
```

Wait 10–30 seconds for the OTEL Collector to batch and ship the logs, then retry the check.

---

## How Logbook HTTP Logs Land in Elasticsearch

The application uses [Logbook](https://github.com/zalando/logbook) to log HTTP requests and
responses as structured JSON. When the `json-logs` profile is active, each HTTP response produces
a log entry with the following fields in Elasticsearch (stored flat due to OTEL raw mapping mode):

| Field | Type | Description |
|---|---|---|
| `http.status` | number | HTTP response status code (200, 404, 500, …) |
| `http.type` | keyword | `"request"` or `"response"` |
| `http.method` | keyword | HTTP method (GET, POST, …) |
| `http.uri` | keyword | Full request URI |
| `http.path` | keyword | Request path |
| `http.duration` | number | Response time in milliseconds |
| `http.correlation` | keyword | Correlates request + response log pairs |
| `logger_name` | keyword | Always `org.zalando.logbook.Logbook` for HTTP logs |
| `@timestamp` | date | When the response was logged |

> **Note:** Only response logs (where `http.type = "response"`) have the `http.status` field.
> The dashboard filters to these entries only.

---

## Step 1 — Create a Data View

A Data View tells Kibana which Elasticsearch index to query and which field to use as the timestamp.

1. Open Kibana at **http://localhost:5601**
2. Click the **hamburger menu** (☰) in the top-left corner
3. Navigate to **Stack Management** → **Data Views**
4. Click **Create data view**
5. Fill in the form:
   - **Name**: `Library Logs`
   - **Index pattern**: `logs-*`
   - **Timestamp field**: `@timestamp`
6. Click **Save data view to Kibana**

> **Tip:** If you are unsure of the exact index name, check it first:
> ```bash
> curl "http://localhost:9200/_cat/indices?v&h=index,docs.count"
> ```
> Look for an index like `.ds-logs-generic-default-2026.02.25-000001`. The pattern `logs-*` matches it.

---

## Step 2 — Create the Donut Chart (Status Distribution)

This panel shows the percentage share of each HTTP status code.

1. Click the hamburger menu (☰) → **Dashboards**
2. Click **Create dashboard**
3. Click **Create visualization** (the blue button in the empty canvas)
4. The **Lens** editor opens. In the top-left, confirm the data view is **Library Logs**

### Configure the chart type

5. In the top-right of the Lens editor, click the chart type dropdown (currently shows "Bar vertical stacked" or similar)
6. Select **Donut**

### Add the query filter

7. In the **KQL query bar** at the top of the editor, type:
   ```
   logger_name: "org.zalando.logbook.Logbook"
   ```
8. Press **Enter**

### Add a filter for responses only

9. Click **+ Add filter** (below the query bar)
10. Set **Field** to `http.type`, **Operator** to `is`, **Value** to `response`
11. Set **Label** (optional) to `HTTP responses only`
12. Click **Save**

### Configure the slice dimension

13. In the right panel under **Slice by**, click **+ Add or drag-and-drop a field**
14. In the field search box, type `http.status` and select it
15. The column type auto-selects **Top values**. Configure it:
    - **Number of values**: `10`
    - **Order by**: `Count of records` (descending)
    - **Label**: `HTTP Status`
16. Click **Close**

### Configure the size metric

17. Under **Size by**, **Count of records** should already be present. If not:
    - Click **+ Add or drag-and-drop a field**
    - Select **Count of records**

### Configure display

18. Under **Number format**, select **Percent**
19. Ensure **Show values** is enabled so percentages appear on slices

### Save the panel

20. Click **Save and return** (top-right)
21. The donut chart appears on the dashboard canvas

---

## Step 3 — Create the Data Table (Count + Avg Duration per Status)

This panel shows a tabular breakdown: status code, request count, and average response time.

1. On the dashboard canvas, click **Create visualization** again
2. In the Lens editor, confirm data view is **Library Logs**

### Set chart type

3. Click the chart type dropdown → select **Table**

### Add the KQL filter

4. In the query bar, type:
   ```
   logger_name: "org.zalando.logbook.Logbook"
   ```
5. Press **Enter**
6. Add a filter: **http.type** `is` `response` → **Save**

### Configure the rows dimension (status code column)

7. Under **Rows**, click **+ Add or drag-and-drop a field**
8. Search for and select `http.status`
9. Configure:
   - **Number of values**: `20`
   - **Order by**: `Count of records` (descending)
   - **Label**: `HTTP Status Code`
10. Click **Close**

### Add the count metric column

11. Under **Metrics**, click **+ Add or drag-and-drop a field**
12. Select **Count of records**
13. Set **Label** to `Request Count`
14. Click **Close**

### Add the average duration metric column

15. Under **Metrics**, click **+ Add or drag-and-drop a field**
16. Search for `http.duration` and select it
17. The aggregation auto-selects **Average**. Configure:
    - **Label**: `Avg Duration (ms)`
18. Click **Close**

### Save the panel

19. Click **Save and return**
20. The table panel appears on the dashboard canvas

---

## Step 4 — Create the Bar Chart (Status Over Time)

This panel shows request volume per status code as a stacked bar chart over time.

1. On the dashboard canvas, click **Create visualization**
2. Confirm data view is **Library Logs**

### Set chart type

3. Click the chart type dropdown → select **Bar vertical stacked**

### Add the KQL filter

4. Query bar: `logger_name: "org.zalando.logbook.Logbook"` → Enter
5. Add filter: **http.type** `is` `response` → **Save**

### Configure the horizontal axis (time)

6. Under **Horizontal axis**, click **+ Add or drag-and-drop a field**
7. Search for `@timestamp` and select it
8. The aggregation auto-selects **Date histogram**. Configure:
   - **Minimum interval**: `Auto`
9. Click **Close**

### Configure the vertical axis (count)

10. Under **Vertical axis**, **Count of records** should appear automatically. If not:
    - Click **+ Add or drag-and-drop a field** → select **Count of records**
11. Set **Label** to `Count`

### Configure the breakdown (one series per status code)

12. Under **Break down by**, click **+ Add or drag-and-drop a field**
13. Search for `http.status` and select it
14. Configure:
    - **Number of values**: `5`
    - **Order by**: `Count of records` (descending)
    - **Label**: `HTTP Status`
15. Click **Close**

### Configure legend

16. Scroll down in the right panel to **Legend**
17. Set **Display** to `Show` and **Position** to `Bottom`

### Save the panel

18. Click **Save and return**

---

## Step 5 — Arrange the Dashboard Layout

Arrange the three panels so the most important information is visible without scrolling.

1. **Drag** the **donut chart** to the top-left, resize it to roughly half the screen width
2. **Drag** the **data table** to the top-right, resize it to fill the remaining top half
3. **Drag** the **bar chart** to the bottom row, stretch it full width

Panels can be resized by dragging the **resize handle** at the bottom-right corner of each panel.

---

## Step 6 — Set the Dashboard Time Range

1. In the top-right corner of the dashboard, click the **time picker** (e.g., "Last 15 minutes")
2. Select a range appropriate for your data, e.g., **Last 1 hour** or **Today**
3. To auto-refresh, click the **Refresh** dropdown next to the time picker and choose an interval
   (e.g., every 30 seconds) so the dashboard updates as new requests arrive

---

## Step 7 — Save the Dashboard

1. Click **Save** in the top-right corner
2. Fill in:
   - **Title**: `HTTP Request Status Distribution`
   - **Description**: `Monitor distribution of HTTP requests by response status code`
3. Click **Save**

The dashboard is now saved and accessible from **Dashboards** in the left navigation.

---

## Final Dashboard Layout

```
┌─────────────────────────────┬──────────────────────────────┐
│  HTTP Response Status       │  Request Count by Status     │
│  Distribution               │  Code                        │
│                             │                              │
│       [Donut chart]         │  Status │ Count │ Avg ms     │
│    200 ■ 15%  404 ■ 85%     │  404    │  11   │  2.2       │
│                             │  200    │   2   │  82.5      │
│                             │                              │
├─────────────────────────────┴──────────────────────────────┤
│  HTTP Requests by Status Over Time                         │
│                                                            │
│  12 ┤                                                      │
│  10 ┤   ████                                               │
│   8 ┤   ████                                               │
│   6 ┤   ████                                               │
│   4 ┤   ████  ██                                           │
│   2 ┤   ████  ██                                           │
│   0 └───────────────────── time ──────────────────────     │
│  ● 404  ● 200                                              │
└────────────────────────────────────────────────────────────┘
```

---

## Troubleshooting

### No data appears in the panels

- Verify the application is running with the `json-logs` profile active. Logbook HTTP logging
  only produces structured `http.*` fields when `logbook.format.style=json` is configured.
  Check `application-json-logs.yaml` — it sets this automatically when the profile is active.
- Check the time range: the time picker defaults to "Last 15 minutes". If you generated traffic
  earlier, widen the range to "Last 24 hours".
- Confirm the OTEL Collector is running and connected to Elasticsearch:
  ```bash
  curl "http://localhost:9200/_cat/indices?v&h=index,docs.count"
  ```
  The `docs.count` for `.ds-logs-generic-default-*` should be non-zero.

### The `http.status` field is not found in Kibana

- After the first HTTP log entry is indexed, Kibana needs to refresh the data view field list.
  Go to **Stack Management** → **Data Views** → **Library Logs** → click **Refresh field list**
  (the circular arrow icon in the top-right of the field list).

### Admin endpoints (actuator/prometheus) appear in results

- The application already excludes `/admin/**` from Logbook logging via `application.yaml`.
  No additional filtering is needed.

### The donut chart shows "No results found"

- Make sure the filter `http.type: response` is applied. Request logs do not carry the
  `http.status` field — only response logs do.
