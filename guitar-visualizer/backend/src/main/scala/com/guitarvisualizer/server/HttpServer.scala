package com.guitarvisualizer.server

import com.guitarvisualizer.config.AppConfig
import com.guitarvisualizer.database.DatabaseConnection
import com.guitarvisualizer.repository.ChordRepository
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{ChannelFuture, ChannelInitializer}
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import org.slf4j.LoggerFactory

class HttpServer(config: AppConfig) {
  private val logger = LoggerFactory.getLogger(getClass)
  private var channelFuture: Option[ChannelFuture] = None
  private val bossGroup = new NioEventLoopGroup(1)
  private val workerGroup = new NioEventLoopGroup()
  
  def start(): Unit = {
    try {
      // Initialize database connection
      val db = DatabaseConnection.initialize(config.database)
      val repository = new ChordRepository(db)
      
      val bootstrap = new ServerBootstrap()
      bootstrap.group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new ChannelInitializer[SocketChannel] {
          override def initChannel(ch: SocketChannel): Unit = {
            ch.pipeline()
              .addLast(new HttpServerCodec())
              .addLast(new HttpObjectAggregator(65536))
              .addLast(new HttpRequestHandler(repository))
          }
        })
      
      val future = bootstrap.bind(config.server.host, config.server.port).sync()
      channelFuture = Some(future)
      
      logger.info(s"Server started on http://${config.server.host}:${config.server.port}")
      
      // Wait for the server socket to close
      future.channel().closeFuture().sync()
      
    } finally {
      stop()
    }
  }
  
  def stop(): Unit = {
    logger.info("Stopping HTTP server...")
    
    channelFuture.foreach(_.channel().close())
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
    
    // Close database connection
    try {
      DatabaseConnection.get.close()
    } catch {
      case _: Exception => // ignore
    }
    
    logger.info("HTTP server stopped")
  }
}
