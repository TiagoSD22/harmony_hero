package com.guitarvisualizer

import com.guitarvisualizer.config.AppConfig
import com.guitarvisualizer.database.DatabaseMigration
import com.guitarvisualizer.seeder.ChordDataSeeder
import com.guitarvisualizer.server.HttpServer
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

object Main {
  private val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    logger.info("Starting Guitar Chord Visualizer Backend...")

    val config = AppConfig.load()
    
    Try {
      // Initialize database and run migrations
      logger.info("Running database migrations...")
      DatabaseMigration.migrate(config.database)
      
      // Seed chord data if needed
      logger.info("Seeding chord data...")
      ChordDataSeeder.seedIfEmpty(config.database)
      
      // Start HTTP server
      logger.info(s"Starting HTTP server on port ${config.server.port}...")
      val server = new HttpServer(config)
      server.start()
      
      // Add shutdown hook
      Runtime.getRuntime.addShutdownHook(new Thread(() => {
        logger.info("Shutting down server...")
        server.stop()
      }))
      
      logger.info("Server started successfully!")
      
    } match {
      case Success(_) => 
        logger.info("Application started successfully")
      case Failure(exception) => 
        logger.error("Failed to start application", exception)
        System.exit(1)
    }
  }
}
