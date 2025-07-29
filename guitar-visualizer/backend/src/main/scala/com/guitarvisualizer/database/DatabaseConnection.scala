package com.guitarvisualizer.database

import com.guitarvisualizer.config.DatabaseConfig
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.slf4j.LoggerFactory

import java.sql.Connection
import javax.sql.DataSource

class DatabaseConnection(config: DatabaseConfig) {
  private val logger = LoggerFactory.getLogger(getClass)
  
  private val dataSource: DataSource = {
    val hikariConfig = new HikariConfig()
    hikariConfig.setJdbcUrl(config.url)
    hikariConfig.setUsername(config.username)
    hikariConfig.setPassword(config.password)
    hikariConfig.setMaximumPoolSize(config.maxPoolSize)
    hikariConfig.setDriverClassName("org.postgresql.Driver")
    
    // Connection pool settings
    hikariConfig.setConnectionTimeout(30000)
    hikariConfig.setIdleTimeout(600000)
    hikariConfig.setMaxLifetime(1800000)
    
    logger.info(s"Initializing database connection pool for ${config.url}")
    new HikariDataSource(hikariConfig)
  }
  
  def getConnection: Connection = dataSource.getConnection
  
  def close(): Unit = {
    dataSource match {
      case hikari: HikariDataSource => hikari.close()
      case _ => // no-op
    }
  }
}

object DatabaseConnection {
  private var instance: Option[DatabaseConnection] = None
  
  def initialize(config: DatabaseConfig): DatabaseConnection = {
    instance.foreach(_.close())
    val connection = new DatabaseConnection(config)
    instance = Some(connection)
    connection
  }
  
  def get: DatabaseConnection = {
    instance.getOrElse(throw new IllegalStateException("Database connection not initialized"))
  }
}
