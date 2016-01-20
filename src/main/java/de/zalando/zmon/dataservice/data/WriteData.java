package de.zalando.zmon.dataservice.data;

import java.util.Optional;

import javax.annotation.concurrent.Immutable;

@Immutable
class WriteData {

    private final Optional<WorkerResult> workerResultOptional;
    private final String accountId;
    private final int checkId;
    private final String data;

    WriteData(Optional<WorkerResult> workerResultOptional, String accountId, int checkId, String data) {
        this.workerResultOptional = workerResultOptional;
        this.accountId = accountId;
        this.checkId = checkId;
        this.data = data;
    }

    public Optional<WorkerResult> getWorkerResultOptional() {
        return workerResultOptional;
    }

    public String getAccountId() {
        return accountId;
    }

    public int getCheckId() {
        return checkId;
    }

    public String getData() {
        return data;
    }

}
