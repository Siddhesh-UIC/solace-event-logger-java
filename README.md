# solace-event-logger-java

A Java microservice that subscribes to a Solace PubSub+ broker and writes every received message to structured log files. Supports both Guaranteed (queue-bound) and Direct (topic-subscribed) consumption with pluggable output formats.

---

## Requirements

| Requirement | Version |
|-------------|---------|
| Java (JDK)  | 17+     |
| Maven       | 3.8+    |
| Solace PubSub+ broker | 9.x / 10.x |

---

## Build

```bash
mvn package -DskipTests
```

Produces `target/message-logger-1.0.0.jar` — a self-contained fat JAR with all dependencies bundled.

To build and run tests:

```bash
mvn package
```

Run only the tests:

```bash
mvn test
```

---

## Run

### Using the helper script (Linux / macOS / WSL)

```bash
chmod +x run.sh
./run.sh
```

The script loads a `.env` file from the current directory if one exists, then executes the JAR with `config/application.yaml` as the config path.

### Directly with Java

```bash
java -jar target/message-logger-1.0.0.jar --config config/application.yaml
```

### Config path resolution (in priority order)

1. `--config <path>` command-line argument
2. `CONFIG_PATH` environment variable
3. Default: `./config/application.yaml`

### Minimum required environment variables

| Variable           | Description                          |
|--------------------|--------------------------------------|
| `SOLACE_PASSWORD`  | Broker password — no default, **required** |
| `SOLACE_HOST`      | Broker host (default: `tcp://broker.example.com:55555`) |
| `SOLACE_VPN`       | Message VPN (default: `BVPN`) |
| `SOLACE_USERNAME`  | Client username (default: `logger-svc`) |

Set these in a `.env` file or export them before running:

```bash
export SOLACE_HOST=tcp://my-broker:55555
export SOLACE_VPN=prod
export SOLACE_USERNAME=logger-svc
export SOLACE_PASSWORD=secret
./run.sh
```

---

## Configuration Reference (`application.yaml`)

All values support `${ENV_VAR:-default}` placeholder syntax. Self-references like `${service.name}` are also resolved.

### `service`

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `name` | string | `solace-message-logger` | Human-readable service name |
| `version` | string | `1.0.0` | Informational only |
| `config-version` | int | `1` | Service warns at startup if this differs from its expected version |
| `instance-id` | string | `${HOSTNAME:-default-instance}` | Identifies this instance in logs |
| `timezone` | string | `Asia/Kolkata` | Timezone used when formatting timestamps in output files |

---

### `solace`

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `host` | string | `tcp://broker.example.com:55555` | Broker TCP endpoint (`tcp://host:port`) or SMF URL |
| `vpn` | string | `BVPN` | Message VPN name |
| `username` | string | `logger-svc` | Client username |
| `password` | string | **required** | Client password — no default |
| `client-name` | string | `{service.name}-{instance-id}` | JCSMP client name, must be unique per session |

#### `solace.connection`

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `connect-retries` | int | `5` | Retries on initial connect before giving up |
| `connect-retries-per-host` | int | `3` | Retries per host when multiple hosts are specified |
| `reconnect-retries` | int | `-1` | Retries after a dropped session; `-1` = infinite |
| `reconnect-retry-wait-ms` | int | `3000` | Milliseconds between reconnect attempts |
| `keep-alive-interval-ms` | int | `3000` | How often to send keep-alive frames |
| `keep-alive-limit` | int | `3` | Missed keep-alives before the session is declared dead |
| `tcp-no-delay` | bool | `true` | Disables Nagle's algorithm — reduces latency |
| `compression-level` | int | `0` | `0` = off; `1`–`9` = zlib level (use on WAN links) |

#### `solace.connection.ssl`

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `enabled` | bool | `false` | Enable TLS/SSL transport |
| `trust-store` | string | `""` | Path to JKS/PKCS12 trust store |
| `trust-store-password` | string | `""` | Trust store password |
| `validate-certificate` | bool | `true` | Reject brokers with untrusted certificates |

---

### `logging.service`

Controls the service's own operational log (lifecycle events, warnings, errors). Distinct from the message output files.

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `enabled` | bool | `true` | Enable service log output |
| `directory` | string | `./logs/service` | Directory for rolling log files |
| `file-pattern` | string | `service-{yyyyMMdd}.log` | File name pattern |
| `level` | string | `INFO` | Log level: `TRACE` \| `DEBUG` \| `INFO` \| `WARN` \| `ERROR`. Also reads `LOG_LEVEL` env var |
| `rolling` | string | `daily` | Rotation policy (currently only `daily` supported) |
| `max-size-mb` | int | `100` | Maximum size per log file before forced rotation |
| `max-history-days` | int | `30` | Days of rolled files to retain |
| `encoding` | string | `UTF-8` | File character encoding |
| `pattern` | string | `[{timestamp}] [{level}] [{thread}] [{logger}] - {message}` | Log line format. Placeholders: `{timestamp}`, `{level}`, `{thread}`, `{logger}`, `{message}` |
| `console-also` | bool | `true` | Mirror log output to stdout |

---

### `consumer`

#### `consumer.mode`

| Value | Description |
|-------|-------------|
| `GUARANTEED` | Binds to a durable queue. Messages are broker-spooled, acknowledged after successful write |
| `DIRECT` | Subscribes directly to topics. No ack, no spooling — best-effort delivery |

---

#### `consumer.guaranteed` — Guaranteed mode settings

##### `consumer.guaranteed.queue`

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `name` | string | `Q/microgateway/logger` | Queue name on the broker |
| `access-type` | string | `EXCLUSIVE` | `EXCLUSIVE` (one consumer) or `NON_EXCLUSIVE` (competing consumers) |
| `ack-mode` | string | `CLIENT` | `CLIENT` = ack after write; `AUTO` = ack immediately on receive |
| `max-redelivery` | int | `5` | Max redelivery attempts before the broker sends to DMQ |
| `flow-window-size` | int | `255` | Unacknowledged messages the broker can push at once |
| `transport-window-size` | int | `255` | JCSMP transport window |
| `start-state` | string | `ENABLED` | `ENABLED` starts consuming immediately |
| `provision` | bool | `false` | `true` = create the queue on startup if it does not exist |
| `respect-existing` | bool | `true` | When provisioning, leave an existing queue unchanged |

##### `consumer.guaranteed.queue.subscriptions[]`

| Key | Type | Description |
|-----|------|-------------|
| `topic` | string | Topic subscription string added to the queue (supports `>` wildcard) |
| `add-on-startup` | bool | `true` = add the subscription if missing; `false` = assume it is already managed externally |

---

#### `consumer.direct` — Direct mode settings

| Key | Type | Description |
|-----|------|-------------|
| `subscriptions` | list of strings | Topic strings to subscribe to (supports `>` and `*` wildcards) |

---

#### `consumer.output` — Message output writer

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `enabled` | bool | `true` | Enable writing output files |
| `directory` | string | `./logs/messages` | Directory for message output files |
| `file-pattern` | string | `messages-{yyyyMMdd}.{format}` | Output file name pattern; `{format}` is replaced with the format extension |
| `format` | string | `jsonl` | Output format: `jsonl` \| `csv` \| `txt` \| `log` |
| `rolling` | string | `daily` | Rotation policy |
| `max-size-mb` | int | `500` | Max file size before forced rotation |
| `max-history-days` | int | `90` | Days of rotated output files to retain |
| `encoding` | string | `UTF-8` | Output file character encoding |
| `capture-schema` | string | `./config/capture-schema.json` | Path to the field selection schema |

##### `consumer.output.delimited` — CSV / delimited format options

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `delimiter` | string | `\|` | Field separator character |
| `quote-char` | string | `"` | Quote character (RFC-4180; fields containing the delimiter, quote, or newline are auto-quoted) |
| `include-header` | bool | `true` | Write a header row on new/rotated files (CSV only) |

##### `consumer.output.log-format` — Log format options

| Key | Type | Description |
|-----|------|-------------|
| `pattern` | string | Line format for the `log` output mode. Use `{field_name}` placeholders matching field names in the capture schema. Tokens for disabled fields are silently removed from the line |

##### `consumer.output.flush` — Flush / sync policy

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `every-records` | int | `1000` | Flush the output buffer after this many records (`0` = never flush mid-file) |
| `every-ms` | int | `500` | Maximum milliseconds between flushes |
| `fsync-on-rotate` | bool | `true` | Call `fsync` on the file descriptor before closing on rotation |

##### `consumer.output.filter` — Topic and payload filtering

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `include-topics` | list of strings | `[">"]` | Only messages whose topic matches one of these patterns are written. `>` matches everything |
| `exclude-topics` | list of strings | | Topics matching any pattern here are dropped even if they match an include pattern |
| `max-payload-bytes` | int | `1048576` | Maximum payload size accepted (1 MB default) |
| `truncate-payload` | bool | `true` | If `true`, oversized payloads are truncated; if `false`, the message is skipped |
| `skip-empty-payload` | bool | `false` | Drop messages that carry no payload |

---

#### `consumer.pipeline` — Internal processing pipeline

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `consumer-threads` | int | `1` | Threads reserved for consumer callbacks (informational) |
| `writer-threads` | int | `2` | Parallel threads draining the internal queue to the writer |
| `internal-queue-capacity` | int | `50000` | Bounded in-memory queue between consumer callbacks and writer threads |
| `batch-size` | int | `500` | Maximum records per write batch |
| `batch-timeout-ms` | int | `100` | Maximum milliseconds to wait before flushing a partial batch |

---

## Output Formats

| Format | Extension | Description |
|--------|-----------|-------------|
| `jsonl` | `.jsonl` | One JSON object per line (JSON Lines). Fields are only included when enabled in the capture schema |
| `csv` | `.csv` | RFC-4180 delimited file. Header row is written on each new/rotated file when `include-header: true` |
| `txt` | `.txt` | Key=value pairs, one message per blank-line-delimited block |
| `log` | `.log` | Single-line log entries using the pattern defined in `log-format.pattern` |

---

## Capture Schema (`capture-schema.json`)

Controls which message fields are written to the output file. Fields with `"enabled": false` are excluded from all output formats.

```json
{
  "schema_version": 1,
  "fields": [
    { "name": "timestamp",                    "enabled": true  },
    { "name": "receive_timestamp",            "enabled": true  },
    { "name": "sender_timestamp",             "enabled": false },
    { "name": "broker_timestamp",             "enabled": true  },
    { "name": "message_id",                   "enabled": true  },
    { "name": "correlation_id",               "enabled": true  },
    { "name": "replication_group_message_id", "enabled": true  },
    { "name": "destination_topic",            "enabled": true  },
    { "name": "queue_name",                   "enabled": true  },
    { "name": "delivery_mode",                "enabled": true  },
    { "name": "priority",                     "enabled": false },
    { "name": "redelivered",                  "enabled": true  },
    { "name": "dmq_eligible",                 "enabled": true  },
    { "name": "expiration",                   "enabled": false },
    { "name": "payload_size_bytes",           "enabled": true  },
    { "name": "content_type",                 "enabled": true  },
    { "name": "http_method",                  "enabled": true  },
    { "name": "http_uri",                     "enabled": true  },
    { "name": "http_status",                  "enabled": true  },
    { "name": "headers",                      "enabled": true  },
    { "name": "user_properties",              "enabled": true  },
    { "name": "payload",                      "enabled": true  },
    { "name": "payload_encoding",             "enabled": false }
  ]
}
```

| Field | Description |
|-------|-------------|
| `timestamp` | Wall-clock time the message was processed (ISO-8601) |
| `receive_timestamp` | Same as `timestamp` (receive time) |
| `sender_timestamp` | Timestamp set by the publisher (may be absent) |
| `broker_timestamp` | Timestamp stamped by the broker |
| `message_id` | Application message ID (`setApplicationMessageId`) |
| `correlation_id` | Correlation ID for request/reply patterns |
| `replication_group_message_id` | Broker-assigned RGMID for replication tracking |
| `destination_topic` | Topic the message was published to |
| `queue_name` | Queue the message was consumed from (Guaranteed only) |
| `delivery_mode` | `PERSISTENT`, `NON_PERSISTENT`, or `DIRECT` |
| `priority` | Message priority (0–255) |
| `redelivered` | `true` if this is a redelivery attempt |
| `dmq_eligible` | Whether the message can be moved to the Dead Message Queue |
| `expiration` | Epoch-millisecond expiry timestamp (`0` = no expiry) |
| `payload_size_bytes` | Raw payload size in bytes |
| `content_type` | HTTP content-type header (REST Delivery Point messages) |
| `http_method` | HTTP method (RDP messages) |
| `http_uri` | HTTP URI (RDP messages) |
| `http_status` | HTTP status code (RDP messages) |
| `headers` | Solace message headers serialised as JSON |
| `user_properties` | User-defined SDT map properties serialised as JSON |
| `payload` | Message body (UTF-8 text or base64 if binary) |
| `payload_encoding` | `utf-8` or `base64` — indicates how `payload` is encoded |

---

## Dependencies

### Runtime

| Dependency | Version | Purpose |
|------------|---------|---------|
| `com.solacesystems:sol-jcsmp` | 10.25.0 | Solace JCSMP client — session, flow, and message APIs |
| `org.yaml:snakeyaml` | 2.2 | First-pass YAML parse for `${VAR:-default}` placeholder resolution |
| `com.fasterxml.jackson.core:jackson-databind` | 2.17.1 | JSON serialisation (JSONL output, user properties, headers) |
| `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` | 2.17.1 | YAML-to-POJO binding for `AppConfig` |
| `ch.qos.logback:logback-classic` | 1.5.6 | Service operational logging (SLF4J implementation) |

### Test only

| Dependency | Version | Purpose |
|------------|---------|---------|
| `org.junit.jupiter:junit-jupiter` | 5.10.2 | JUnit 5 test framework |
| `org.mockito:mockito-core` | 5.11.0 | Mock objects for Solace JCSMP types |
| `org.mockito:mockito-junit-jupiter` | 5.11.0 | JUnit 5 + Mockito integration (`@ExtendWith(MockitoExtension.class)`) |

### Build plugins

| Plugin | Version | Purpose |
|--------|---------|---------|
| `maven-compiler-plugin` | 3.13.0 | Compiles Java 17 source |
| `maven-surefire-plugin` | 3.2.5 | Runs JUnit 5 tests |
| `maven-shade-plugin` | 3.5.3 | Packages fat JAR with `Implementation-Version` manifest entry |
