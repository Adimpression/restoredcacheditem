package restoredcacheditem.restoredcacheditem

import io.grpc.StatusRuntimeException
import io.grpc.inprocess.InProcessChannelBuilder
import main.Test
import restoredcacheditem.restoredcacheditem.NotRestoredCachedItem
import restoredcacheditem.restoredcacheditem.ToIsRestoredCachedItemGrpc
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class ToIsRestoredCachedItemImplBaseImplTest extends Specification {

    @Shared
    ToIsRestoredCachedItemGrpc.ToIsRestoredCachedItemBlockingStub stub

    def setupSpec() {
        Test.before()
        stub = ToIsRestoredCachedItemGrpc.newBlockingStub(InProcessChannelBuilder.forName(ToIsRestoredCachedItemGrpc.SERVICE_NAME).usePlaintext().build()).withWaitForReady()
    }

    def """Should not allow empty"""() {
        setup:
        def item = NotRestoredCachedItem.newBuilder().build()

        when:
        stub.produce(item)

        then:
        thrown StatusRuntimeException
    }
}
