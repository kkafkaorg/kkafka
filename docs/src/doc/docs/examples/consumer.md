# Consumer

Rather than writing a polling loop, KKafka allows you to simply receive records as
an [asynchronous Flow](https://kotlinlang.org/docs/flow.html).

Because Flows are
[cold](https://elizarov.medium.com/cold-flows-hot-channels-d74769805f9)
and sequential, they are great to process records without having to worry about the mechanics of when to poll for
records:

```kotlin
val props = Properties().apply {
    setProperty("enable.auto.commit", "true")
    // and your other settings
}

val consumer = KafkaConsumer<String, String>(props)

var active = true

consumer.subscribe(listOf("foo", "bar"))
val flow = consumer.pollWithFlattenedFlow()
    .takeWhile { active }
    .onEach {
        println("Received text $it")
    }
    .filter { (_, v) ->
        v.isBlank()
    }
    .onEach { (k, v) ->
        insertIntoDb(k, v)
    }
    .collect()

```

Note how:

- We use higher order functions like `filter()`, rather than stateful loops

- We are able to call other suspending functions while processing our records

- The flow is cold: we will only poll for the next record once we are done with the current one
  
- We can cleanly stop the polling at any time by setting `active = false`.

