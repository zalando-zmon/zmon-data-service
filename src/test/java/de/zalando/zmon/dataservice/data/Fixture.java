package de.zalando.zmon.dataservice.data;

import java.util.Optional;

class Fixture {

    static WriteData writeData(Optional<WorkerResult> workerResultOptional) {
        return new WriteData(workerResultOptional, "stups", 13, "{}");
    }
}
