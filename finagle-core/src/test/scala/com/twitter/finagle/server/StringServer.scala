package com.twitter.finagle.server

import com.twitter.finagle._
import com.twitter.finagle.param
import com.twitter.finagle.dispatch.SerialServerDispatcher
import com.twitter.finagle.netty3.Netty3Listener
import com.twitter.finagle.transport.Transport
import java.nio.charset.StandardCharsets
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.frame.{Delimiters, DelimiterBasedFrameDecoder}
import org.jboss.netty.handler.codec.string.{StringEncoder, StringDecoder}

private[finagle] object StringServerPipeline extends ChannelPipelineFactory {
  def getPipeline = {
    val pipeline = Channels.pipeline()
    pipeline.addLast("line", new DelimiterBasedFrameDecoder(100, Delimiters.lineDelimiter: _*))
    pipeline.addLast("stringDecoder", new StringDecoder(StandardCharsets.UTF_8))
    pipeline.addLast("stringEncoder", new StringEncoder(StandardCharsets.UTF_8))
    pipeline
  }
}

private[finagle] object StringServer {
  val protocolLibrary = "string"
}

trait StringServer {
  import StringServer._

  case class Server(
    stack: Stack[ServiceFactory[String, String]] = StackServer.newStack,
    params: Stack.Params = StackServer.defaultParams + param.ProtocolLibrary(protocolLibrary)
  ) extends StdStackServer[String, String, Server] {
    protected def copy1(
      stack: Stack[ServiceFactory[String, String]] = this.stack,
      params: Stack.Params = this.params
    ) = copy(stack, params)

    protected type In = String
    protected type Out = String

    protected def newListener() = Netty3Listener(StringServerPipeline, params)
    protected def newDispatcher(
      transport: Transport[In, Out],
      service: Service[String, String]
    ) = new SerialServerDispatcher(transport, service)
  }

  val stringServer = Server()
}
