# Kafka Booking Workflow

This module now contains a real Kafka-backed booking workflow that can be explained, demoed, and listed on a resume.

## What It Does

When booking APIs change booking state, the app publishes lifecycle events to Kafka.

Supported event types:

- `BOOKING_CREATED`
- `BOOKING_CANCELLED`
- `BOOKING_PAYMENT_UPDATED`

The consumer stores each event in MongoDB and also updates a latest-state snapshot per booking.

## End-to-End Flow

1. A client calls a booking endpoint such as `POST /api/bookings`.
2. `BookingController` validates the request and writes the main booking record to MongoDB.
3. The controller builds a `BookingWorkflowEvent` payload with booking id, turf id, user id, owner id, request id, event type, amount, booking status, and payment status.
4. `BookingWorkflowPublisher` sends the event to Kafka topic `criczone.booking.workflow.v1` when Kafka is enabled.
5. `BookingWorkflowConsumer` reads the event from Kafka.
6. `BookingWorkflowProjectionService` writes:
   - a timeline entry into `booking_workflow_events`
   - a latest-state view into `booking_workflow_snapshots`
7. Workflow read endpoints expose the result for demo and debugging.

## Collections Created

- `bookings`: Source-of-truth booking document.
- `booking_workflow_events`: Immutable event timeline created by the Kafka consumer.
- `booking_workflow_snapshots`: Latest workflow state per booking created by the Kafka consumer.

## API Endpoints

- `POST /api/bookings`: Creates a booking and publishes `BOOKING_CREATED`.
- `PUT /api/bookings/{id}/cancel`: Cancels a booking and publishes `BOOKING_CANCELLED`.
- `PUT /api/bookings/{id}/payment`: Updates payment state and publishes `BOOKING_PAYMENT_UPDATED`.
- `GET /api/bookings/{id}/workflow`: Returns the workflow timeline and latest snapshot for one booking.
- `GET /api/bookings/{id}/history`: Alias for the workflow timeline and latest snapshot, intended for admin/debug demos.
- `GET /api/bookings/workflow/summary`: Returns grouped workflow counts visible to the current user.

## Event Schema

`BookingWorkflowEvent` is the Kafka payload published by the booking service and consumed into MongoDB projections.

| Field | Type | Purpose |
| --- | --- | --- |
| `eventId` | `String` | Unique event id used for idempotency. Duplicate ids are skipped by `existsByEventId`. |
| `bookingId` | `String` | Booking aggregate id and snapshot id. |
| `turfId` | `String` | Turf linked to the booking. |
| `userId` | `String` | User who owns the booking. |
| `ownerId` | `String` | Turf owner, used for owner-specific workflow summaries. |
| `eventType` | `BookingWorkflowEventType` | Lifecycle transition: `BOOKING_CREATED`, `BOOKING_CANCELLED`, or `BOOKING_PAYMENT_UPDATED`. |
| `bookingStatus` | `String` | Current booking status after the transition. |
| `paymentStatus` | `String` | Current payment status after the transition. |
| `amount` | `double` | Booking amount captured for billing projections. |
| `triggeredByUserId` | `String` | User who initiated the transition. |
| `requestId` | `String` | Request tracing id from `RequestTracingFilter`. |
| `source` | `String` | API route that generated the event. |
| `metadata` | `Map<String,Object>` | Transition-specific context such as slot details, invoice number, payment method, or cancellation time. |
| `occurredAt` | `Instant` | Event time used to order timelines. |

## Configuration

Set these environment variables before running the Spring app:

- `KAFKA_ENABLED=true`
- `KAFKA_BOOTSTRAP_SERVERS=localhost:9092`
- `KAFKA_BOOKING_TOPIC=criczone.booking.workflow.v1`
- `KAFKA_CONSUMER_GROUP_ID=criczone-booking-workflow`

If `KAFKA_ENABLED=false`, the app still works, but events are not sent to Kafka and no workflow projection will be built.

## Local Kafka Setup

Start the broker:

```powershell
cd backend
docker compose -f docker-compose.kafka.yml up -d
```

Run the Spring app with Kafka enabled:

```powershell
$env:KAFKA_ENABLED="true"
$env:KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
$env:JWT_SECRET="replace-with-at-least-32-random-bytes"
.\mvnw spring-boot:run
```

## Demo Script

1. Create a booking from the app or by API.
2. Mark payment as `paid`.
3. Open `GET /api/bookings/{id}/workflow` and show that:
   - the main booking exists in `bookings`
   - Kafka-driven timeline entries exist in `booking_workflow_events`
   - the latest workflow state exists in `booking_workflow_snapshots`
4. Cancel another booking and refresh the workflow endpoint to show a new event and new snapshot state.

## Resume-Ready Wording

You can honestly describe this implementation in a few ways:

- Built a Kafka-driven booking workflow in a Spring Boot and MongoDB application, publishing lifecycle events for booking creation, cancellation, and payment updates.
- Implemented asynchronous booking event processing with Spring Kafka consumers that materialize MongoDB workflow timelines and latest-state projections.
- Added request tracing, cache-backed read optimization, and event-driven booking audit visibility for a CricZone sports booking platform.
