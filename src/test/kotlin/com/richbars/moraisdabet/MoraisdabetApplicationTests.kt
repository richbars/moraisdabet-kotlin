//import com.richbars.moraisdabet.core.application.port.BetfairHttpPort
//import io.mockk.coEvery
//import io.mockk.mockk
//import kotlinx.coroutines.runBlocking
//import org.junit.jupiter.api.Test
//
//class MoraisdabetApplicationTests {
//
//	private val betfairHttpPort: BetfairHttpPort = mockk()
//
//	@Test
//	fun `test betfair`() = runBlocking {
//		// Mock do retorno
//		coEvery { betfairHttpPort.getStatusMarketById("1.250481450") } returns "ok!"
//
//		val result = betfairHttpPort.getStatusMarketById("1.250481450")
//		println(result)
//
//		assert(result == "ok!")
//	}
//}
