package com.guitarvisualizer.server

import com.guitarvisualizer.models.JsonCodecs._
import com.guitarvisualizer.models.{ApiResponse, HealthStatus}
import com.guitarvisualizer.repository.ChordRepository
import io.circe.syntax._
import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import io.netty.util.CharsetUtil
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}

class HttpRequestHandler(repository: ChordRepository) extends SimpleChannelInboundHandler[FullHttpRequest] {
  private val logger = LoggerFactory.getLogger(getClass)
  
  override def channelRead0(ctx: ChannelHandlerContext, request: FullHttpRequest): Unit = {
    val uri = request.uri()
    val method = request.method()
    
    logger.debug(s"${method.name()} $uri")
    
    // Handle CORS preflight
    if (method == HttpMethod.OPTIONS) {
      sendCorsResponse(ctx)
      return
    }
    
    // Route the request
    val response = (method, uri) match {
      case (HttpMethod.GET, "/health") =>
        handleHealth()
      case (HttpMethod.GET, uri) if uri.startsWith("/chords/") =>
        handleChordsRequest(uri)
      case _ =>
        createErrorResponse(HttpResponseStatus.NOT_FOUND, "Endpoint not found")
    }
    
    sendJsonResponse(ctx, response._1, response._2)
  }
  
  private def handleHealth(): (HttpResponseStatus, String) = {
    val health = HealthStatus("healthy", System.currentTimeMillis())
    val apiResponse = ApiResponse(success = true, data = Some(health))
    (HttpResponseStatus.OK, apiResponse.asJson.noSpaces)
  }
  
  private def handleChordsRequest(uri: String): (HttpResponseStatus, String) = {
    val pathParts = uri.split("/").filter(_.nonEmpty)
    
    pathParts.toList match {
      case "chords" :: "qualities" :: Nil =>
        // GET /chords/qualities
        repository.findAllQualities() match {
          case Success(qualities) =>
            val response = ApiResponse(success = true, data = Some(qualities))
            (HttpResponseStatus.OK, response.asJson.noSpaces)
          case Failure(ex) =>
            logger.error("Failed to fetch qualities", ex)
            createErrorResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Failed to fetch qualities")
        }
        
      case "chords" :: key :: Nil =>
        // GET /chords/{key}
        repository.findChordsByKey(key) match {
          case Success(chords) if chords.nonEmpty =>
            val response = ApiResponse(success = true, data = Some(chords))
            (HttpResponseStatus.OK, response.asJson.noSpaces)
          case Success(_) =>
            createErrorResponse(HttpResponseStatus.NOT_FOUND, s"No chords found for key: $key")
          case Failure(ex) =>
            logger.error(s"Failed to fetch chords for key: $key", ex)
            createErrorResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Failed to fetch chords")
        }
        
      case "chords" :: key :: quality :: Nil =>
        // GET /chords/{key}/{quality}
        repository.findChordsByKeyAndQuality(key, quality) match {
          case Success(Some(chord)) =>
            val response = ApiResponse(success = true, data = Some(chord))
            (HttpResponseStatus.OK, response.asJson.noSpaces)
          case Success(None) =>
            createErrorResponse(HttpResponseStatus.NOT_FOUND, s"No chord found for key: $key, quality: $quality")
          case Failure(ex) =>
            logger.error(s"Failed to fetch chord for key: $key, quality: $quality", ex)
            createErrorResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Failed to fetch chord")
        }
        
      case _ =>
        createErrorResponse(HttpResponseStatus.NOT_FOUND, "Invalid chord endpoint")
    }
  }
  
  private def createErrorResponse(status: HttpResponseStatus, message: String): (HttpResponseStatus, String) = {
    val response = ApiResponse[String](success = false, error = Some(message))
    (status, response.asJson.noSpaces)
  }
  
  private def sendJsonResponse(ctx: ChannelHandlerContext, status: HttpResponseStatus, content: String): Unit = {
    val response = new DefaultFullHttpResponse(
      HttpVersion.HTTP_1_1,
      status,
      Unpooled.copiedBuffer(content, CharsetUtil.UTF_8)
    )
    
    response.headers()
      .set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8")
      .set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes())
      .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
      .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS")
      .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type, Authorization")
    
    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
  }
  
  private def sendCorsResponse(ctx: ChannelHandlerContext): Unit = {
    val response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
    
    response.headers()
      .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
      .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS")
      .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type, Authorization")
      .set(HttpHeaderNames.CONTENT_LENGTH, 0)
    
    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
  }
  
  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.error("Error processing request", cause)
    
    val errorResponse = ApiResponse[String](success = false, error = Some("Internal server error"))
    sendJsonResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, errorResponse.asJson.noSpaces)
  }
}
