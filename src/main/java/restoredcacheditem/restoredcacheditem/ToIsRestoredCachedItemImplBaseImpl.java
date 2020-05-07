package restoredcacheditem.restoredcacheditem;

import io.grpc.stub.StreamObserver;

import java.util.logging.Logger;

public class ToIsRestoredCachedItemImplBaseImpl extends ToIsRestoredCachedItemGrpc.ToIsRestoredCachedItemImplBase {

    private final Logger log;

    public ToIsRestoredCachedItemImplBaseImpl() {
        log = Logger.getLogger(getClass().getName());
        log.info("starting");

        log.info("started");
    }

    @Override
    public void produce(final NotRestoredCachedItem request, final StreamObserver<IsRestoredCachedItem> responseObserver) {
        responseObserver.onNext(IsRestoredCachedItem.newBuilder()
                .build());
        responseObserver.onCompleted();
    }
}
