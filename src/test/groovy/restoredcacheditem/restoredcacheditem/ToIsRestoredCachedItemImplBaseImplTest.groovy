package restoredcacheditem.restoredcacheditem

import com.google.protobuf.ByteString
import id.id.IsId
import id.output.IsOutput
import identifiedcacheditem.identifiedcacheditem.NotIdentifiedCachedItem
import identifiedcacheditem.identifiedcacheditem.ToIsIdentifiedCachedItemGrpc
import io.grpc.StatusRuntimeException
import io.grpc.inprocess.InProcessChannelBuilder
import main.Test
import removedcacheditem.removedcacheditem.ToIsRemovedCachedItemGrpc
import restoredcacheditem.input.IsInput
import retrievedcacheditem.retrievedcacheditem.NotRetrievedCachedItem
import retrievedcacheditem.retrievedcacheditem.ToIsRetrievedCachedItemGrpc
import spock.lang.Shared
import spock.lang.Specification
import storedcacheditem.storedcacheditem.NotStoredCachedItem
import storedcacheditem.storedcacheditem.ToIsStoredCachedItemGrpc

import java.util.concurrent.TimeUnit

class ToIsRestoredCachedItemImplBaseImplTest extends Specification {

    @Shared
    ToIsRestoredCachedItemGrpc.ToIsRestoredCachedItemBlockingStub stub

    @Shared
    ToIsStoredCachedItemGrpc.ToIsStoredCachedItemBlockingStub toIsStoredCachedItemBlockingStub

    @Shared
    ToIsRemovedCachedItemGrpc.ToIsRemovedCachedItemBlockingStub toIsRemovedCachedItemBlockingStub

    @Shared
    ToIsRetrievedCachedItemGrpc.ToIsRetrievedCachedItemBlockingStub toIsRetrievedCachedItemBlockingStub

    @Shared
    ToIsIdentifiedCachedItemGrpc.ToIsIdentifiedCachedItemBlockingStub toIsIdentifiedCachedItemBlockingStub

    def setupSpec() {
        Test.before()
        toIsStoredCachedItemBlockingStub = ToIsStoredCachedItemGrpc.newBlockingStub(InProcessChannelBuilder.forName(ToIsStoredCachedItemGrpc.SERVICE_NAME).usePlaintext().build())
        toIsRemovedCachedItemBlockingStub = ToIsRemovedCachedItemGrpc.newBlockingStub(InProcessChannelBuilder.forName(ToIsRemovedCachedItemGrpc.SERVICE_NAME).usePlaintext().build())
        toIsRetrievedCachedItemBlockingStub = ToIsRetrievedCachedItemGrpc.newBlockingStub(InProcessChannelBuilder.forName(ToIsRetrievedCachedItemGrpc.SERVICE_NAME).usePlaintext().build())
        toIsIdentifiedCachedItemBlockingStub = ToIsIdentifiedCachedItemGrpc.newBlockingStub(InProcessChannelBuilder.forName(ToIsIdentifiedCachedItemGrpc.SERVICE_NAME).usePlaintext().build())
        stub = ToIsRestoredCachedItemGrpc.newBlockingStub(InProcessChannelBuilder.forName(ToIsRestoredCachedItemGrpc.SERVICE_NAME).usePlaintext().build()).withWaitForReady().withDeadlineAfter(2, TimeUnit.MINUTES)
    }

    def """Should not allow empty"""() {
        setup:
        def item = NotRestoredCachedItem.newBuilder().build()

        when:
        stub.produce(item)

        then:
        def exception = thrown StatusRuntimeException
        assert exception.message == "INVALID_ARGUMENT: 422"

    }

    def """Should not allow empty input"""() {
        setup:
        def item = NotRestoredCachedItem.newBuilder()
                .setIsInput(IsInput.newBuilder().build())
                .build()
        when:
        stub.produce(item)

        then:
        def exception = thrown StatusRuntimeException
        assert exception.message == "INVALID_ARGUMENT: 422"
    }

    def """Should not allow empty input -> id"""() {
        setup:
        def item = NotRestoredCachedItem.newBuilder()
                .setIsInput(IsInput.newBuilder()
                        .setIsId(IsId.newBuilder().build())
                        .build())
                .build()
        when:
        stub.produce(item)

        then:
        def exception = thrown StatusRuntimeException
        assert exception.message == "INVALID_ARGUMENT: 422"
    }

    def """Should not allow empty input -> id -> output"""() {
        setup:
        def item = NotRestoredCachedItem.newBuilder()
                .setIsInput(IsInput.newBuilder()
                        .setIsId(IsId.newBuilder()
                                .setIsOutput(IsOutput.newBuilder().build())
                                .build())
                        .build())
                .build()
        when:
        stub.produce(item)

        then:
        def exception = thrown StatusRuntimeException
        assert exception.message == "INVALID_ARGUMENT: 422"
    }

    def """Should not allow empty input -> id -> output -> string value"""() {
        setup:
        def item = NotRestoredCachedItem.newBuilder()
                .setIsInput(IsInput.newBuilder()
                        .setIsId(IsId.newBuilder()
                                .setIsOutput(IsOutput.newBuilder()
                                        .setIsStringValue("")
                                        .build())
                                .build())
                        .build())
                .build()
        when:
        stub.produce(item)

        then:
        def exception = thrown StatusRuntimeException
        assert exception.message == "INVALID_ARGUMENT: 422"
    }

    def """Should allow replacing existing key"""() {
        setup:
        def key = String.valueOf(System.currentTimeMillis())
        def item = NotRestoredCachedItem.newBuilder()
                .setIsInput(IsInput.newBuilder()
                        .setIsId(IsId.newBuilder()
                                .setIsOutput(IsOutput.newBuilder()
                                        .setIsStringValue(key)
                                        .build())
                                .build())
                        .setIsUseLockBoolean(true)
                        .setIsItemBytes(ByteString.copyFrom("New Value", "UTF-8"))
                        .build())
                .build()
        def notStoredCachedItem = NotStoredCachedItem.newBuilder()
                .setIsInput(storedcacheditem.input.IsInput.newBuilder()
                        .setIsId(IsId.newBuilder()
                                .setIsOutput(IsOutput.newBuilder()
                                        .setIsStringValue(key)
                                        .build())
                                .build())
                        .setIsUseLockBoolean(true)
                        .setIsItemBytes(ByteString.copyFrom(key, "UTF-8"))
                        .build())
                .build()
        def notRetrievedCachedItem = NotRetrievedCachedItem.newBuilder()
                .setIsInput(retrievedcacheditem.input.IsInput.newBuilder()
                        .setIsId(IsId.newBuilder()
                                .setIsOutput(IsOutput.newBuilder()
                                        .setIsStringValue(key)
                                        .build())
                                .build())
                        .setIsUseLockBoolean(true)
                        .build())
                .build()
        when:
        toIsStoredCachedItemBlockingStub.produce(notStoredCachedItem)

        and:
        stub.produce(item)

        and:
        def isRetrievedCachedItem = toIsRetrievedCachedItemBlockingStub.produce(notRetrievedCachedItem)

        then:
        assert isRetrievedCachedItem.getIsOutput().getIsItemBytes().toString("UTF-8") == "New Value"
    }

    def """Should not allow replacing non existing key"""() {
        setup:
        def key = String.valueOf(System.currentTimeMillis())
        def notIdentifiedCachedItem = NotIdentifiedCachedItem.newBuilder()
                .setIsInput(identifiedcacheditem.input.IsInput.newBuilder()
                        .setIsId(IsId.newBuilder()
                                .setIsOutput(IsOutput.newBuilder()
                                        .setIsStringValue(key)
                                        .build())
                                .build())
                        .build())
                .build()
        def item = NotRestoredCachedItem.newBuilder()
                .setIsInput(IsInput.newBuilder()
                        .setIsId(IsId.newBuilder()
                                .setIsOutput(IsOutput.newBuilder()
                                        .setIsStringValue(key)
                                        .build())
                                .build())
                        .setIsUseLockBoolean(true)
                        .setIsItemBytes(ByteString.copyFrom("New Value", "UTF-8"))
                        .build())
                .build()
        when:
        assert !toIsIdentifiedCachedItemBlockingStub.produce(notIdentifiedCachedItem).getIsOutput().getIsKnownBoolean()

        and:
        stub.produce(item)

        then:
        def exception = thrown StatusRuntimeException
        assert exception.message == "INVALID_ARGUMENT: 404"
    }
}
