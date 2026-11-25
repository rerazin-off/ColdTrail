package com.example.mrdetective.data

import com.example.mrdetective.data.network.NetworkErrorHandler
import com.example.mrdetective.data.network.NoConnectionException
import com.example.mrdetective.domain.models.DomainError
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException

/** Tests classification of unavailable connections and server failures. */
class NetworkErrorHandlerTest {
    /** Distinguishes disconnected devices from server errors. */
    @Test
    fun distinguishesDisconnectedDevicesFromServerErrors() {
        val offline = NetworkErrorHandler.map(NoConnectionException()) as DomainError.Network
        val server = NetworkErrorHandler.map(HttpException(Response.error<Unit>(503, "".toResponseBody()))) as DomainError.Network
        val timeout = NetworkErrorHandler.map(SocketTimeoutException()) as DomainError.Network

        assertEquals(DomainError.Network.Kind.Offline, offline.kind)
        assertEquals(DomainError.Network.Kind.Server, server.kind)
        assertEquals(DomainError.Network.Kind.Connection, timeout.kind)
    }
}
