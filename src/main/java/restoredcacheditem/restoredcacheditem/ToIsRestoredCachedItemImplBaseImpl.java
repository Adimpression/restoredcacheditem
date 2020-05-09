package restoredcacheditem.restoredcacheditem;

import com.google.protobuf.ByteString;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import restoredcacheditem.input.IsInput;

import java.util.Map;
import java.util.logging.Logger;

public class ToIsRestoredCachedItemImplBaseImpl extends ToIsRestoredCachedItemGrpc.ToIsRestoredCachedItemImplBase {

    private final Logger logger;

    private final HazelcastInstance hzInstance;
    private final Map<String, ByteString> general;
    private final ILock lock;


    public ToIsRestoredCachedItemImplBaseImpl() {
        logger = Logger.getLogger(getClass().getName());
        logger.info("starting");

        logger.info("starting hazelcast");
        hzInstance = Hazelcast.newHazelcastInstance();
        logger.info("started hazelcast");

        logger.info("starting general map: hazelcast");
        general = hzInstance.getMap("general");
        logger.info("started general map: hazelcast");

        logger.info("starting general lock: hazelcast");
        lock = hzInstance.getLock("general");
        logger.info("started general lock: hazelcast");

        logger.info("started");
    }

    @Override
    public void produce(final NotRestoredCachedItem request, final StreamObserver<IsRestoredCachedItem> responseObserver) {
        try {
            final IsInput isInput;
            final String isStringValue;

            if (!request.hasIsInput()) {
                throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("422"));
            }

            isInput = request.getIsInput();
            if (!isInput
                    .hasIsId()) {
                throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("422"));
            }

            if (!isInput.getIsId()
                    .hasIsOutput()) {
                throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("422"));
            }

            isStringValue = isInput.getIsId()
                    .getIsOutput()
                    .getIsStringValue();

            if (isStringValue.isEmpty()) {
                throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("422"));
            }

            if (isInput.getIsUseLockBoolean()) {
                lock.lock();
                try {
                    if (general.containsKey(isStringValue)) {
                        general.remove(isStringValue);
                        general.put(isStringValue,
                                isInput.getIsItemBytes());
                    } else {
                        throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("404"));
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                if (general.containsKey(isStringValue)) {
                    general.remove(isStringValue);
                    general.put(isStringValue,
                            isInput.getIsItemBytes());
                } else {
                    throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("404"));
                }
            }

            responseObserver.onNext(IsRestoredCachedItem.newBuilder()
                    .build());
            responseObserver.onCompleted();
        } catch (final StatusRuntimeException e) {
            responseObserver.onError(e);
        }
    }
}
