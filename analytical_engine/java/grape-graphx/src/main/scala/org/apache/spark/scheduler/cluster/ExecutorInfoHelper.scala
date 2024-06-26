/*
 * Copyright 2022 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.spark.scheduler.cluster

import org.apache.spark.SparkContext
import org.apache.spark.internal.Logging
import org.apache.spark.scheduler.local.LocalSchedulerBackend

import java.lang.reflect.Field
import java.net.InetAddress
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object ExecutorInfoHelper extends Logging {
  val executorMapFieldName = "org$apache$spark$scheduler$cluster$CoarseGrainedSchedulerBackend$$executorDataMap"

  def getExecutors(sc: SparkContext): mutable.HashMap[String, String] = {
    //executor backend can be several kinds,
    sc.schedulerBackend match {
      case yarnBackend: YarnClusterSchedulerBackend => {
        val field           = getFieldFromYarnBackend(yarnBackend)
        val executorDataMap = getExecutorDataMapReflect(field, yarnBackend)
        executorDatMapToId2Host(executorDataMap)
      }

      case coarseGrainedSchedulerBackend: CoarseGrainedSchedulerBackend => {
        val field           = getFieldFromCoarseBackend(coarseGrainedSchedulerBackend)
        val executorDataMap = getExecutorDataMapReflect(field, coarseGrainedSchedulerBackend)
        executorDatMapToId2Host(executorDataMap)
      }
      // For local mode, there will be only one executor, i.e. driver & executor.
      case local: LocalSchedulerBackend => {
        val res = new mutable.HashMap[String, String]()
        res.+=(("0", InetAddress.getLocalHost.getHostName))
        res
      }
    }
  }

  // TODO check lm
  def getExecutorsHost2Id(sc: SparkContext): mutable.HashMap[String, ArrayBuffer[String]] = {
    //executor backend can be several kinds,
    log.info(s"Got scheduler backend ${sc.schedulerBackend}")
    sc.schedulerBackend match {
      case yarnBackend: YarnClusterSchedulerBackend => {
        val field           = getFieldFromYarnBackend(yarnBackend)
        val executorDataMap = getExecutorDataMapReflect(field, yarnBackend)
        executorDataMapToHost2Id(executorDataMap)
      }

      case coarseGrainedSchedulerBackend: CoarseGrainedSchedulerBackend => {
        val field           = getFieldFromCoarseBackend(coarseGrainedSchedulerBackend)
        val executorDataMap = getExecutorDataMapReflect(field, coarseGrainedSchedulerBackend)
        executorDataMapToHost2Id(executorDataMap)
      }

      // For local mode, there will be only one executor, i.e. driver & executor.
      case local: LocalSchedulerBackend => {
        val res = new mutable.HashMap[String, ArrayBuffer[String]]()
        res.+=((InetAddress.getLocalHost.getHostName, new ArrayBuffer[String].+=("driver")))
        res
      }

      case _ => throw new IllegalStateException("Unsupported backend")
    }
  }

  //*id->available cores
  def getExecutorsCores(sc: SparkContext): mutable.HashMap[String, Int] = {
    //executor backend can be several kinds,
    log.info(s"Got scheduler backend ${sc.schedulerBackend}")
    sc.schedulerBackend match {
      case yarnBackend: YarnClusterSchedulerBackend => {
        val field           = getFieldFromYarnBackend(yarnBackend)
        val executorDataMap = getExecutorDataMapReflect(field, yarnBackend)
        executorDataMap.map(t => (t._1, t._2.freeCores))
      }

      case coarseGrainedSchedulerBackend: CoarseGrainedSchedulerBackend => {
        val field           = getFieldFromCoarseBackend(coarseGrainedSchedulerBackend)
        val executorDataMap = getExecutorDataMapReflect(field, coarseGrainedSchedulerBackend)
        executorDataMap.map(t => (t._1, t._2.freeCores))
      }

      case localSchedulerBackend: LocalSchedulerBackend => {
        val numCores = getNumCoresFromLocalBackend(localSchedulerBackend)
        val res      = new mutable.HashMap[String, Int]
        res.+=(("driver", numCores))
        res
      }

      case _ => throw new IllegalStateException("Unsupported backend")
    }
  }

  def getFieldFromYarnBackend(yarnBackend: YarnClusterSchedulerBackend): Field = {
    log.info(s"Scheduler backend is yarn, total num executors ${yarnBackend.getExecutorIds().mkString(",")}")
    val field = yarnBackend.getClass.getSuperclass.getSuperclass.getDeclaredField(executorMapFieldName)
    require(field != null)
    field
  }

  def getFieldFromCoarseBackend(backend: CoarseGrainedSchedulerBackend): Field = {
    log.info(
      s"Scheduler backend is coarseGrainedBackend, total num executors ${backend.getExecutorIds().mkString(",")}"
    )
    log.info(s"backend.getClass.getSuperclass=${backend.getClass.getSuperclass.toGenericString}")
    log.info(s"backend.getClass.getSuperclass.getDeclaredFields=${backend.getClass.getSuperclass.getDeclaredFields.map(x => x.toGenericString).mkString(",")}")

    log.info(s"backend.getClass.getSuperclass=${backend.getClass.getSuperclass.getSuperclass.toGenericString}")
    log.info(s"backend.getClass.getSuperclass.getDeclaredFields=${backend.getClass.getSuperclass.getSuperclass.getDeclaredFields.map(x => x.toGenericString).mkString(",")}")

    val field = try {
      backend.getClass.getSuperclass.getDeclaredField(executorMapFieldName)
    } catch {
      case e: NoSuchFieldException => backend.getClass.getSuperclass.getSuperclass.getDeclaredField(executorMapFieldName)
    }
    require(field != null)
    field
  }

  def getNumCoresFromLocalBackend(backend: LocalSchedulerBackend): Int = {
    backend.totalCores
  }

  def getExecutorDataMapReflect(
      field: Field,
      castedBackend: CoarseGrainedSchedulerBackend
  ): mutable.HashMap[String, ExecutorData] = {
    field.setAccessible(true)
    val executorDataMap = field
      .get(castedBackend)
      .asInstanceOf[mutable.HashMap[String, ExecutorData]]
    log.info(s"field=${
      field
        .get(castedBackend)
    }")
    log.info(s"castedBackend=${castedBackend.getExecutorIds().mkString(",")}")
    log.info(s"executorDataMap=${executorDataMap.map(x => (x._1, x._2.executorHost)).mkString(",")}")
    require(executorDataMap != null)
    executorDataMap
  }

  def executorDataMapToHost2Id(
      executorDataMap: mutable.HashMap[String, ExecutorData]
  ): mutable.HashMap[String, mutable.ArrayBuffer[String]] = {
    val res = new mutable.HashMap[String, mutable.ArrayBuffer[String]]()
    for (tuple <- executorDataMap) {
      val executorId = tuple._1
      val host       = tuple._2.executorHost
      log.info(s"executor id ${executorId}, host ${host}")
      //here we got ip, cast to hostname
      val hostName = InetAddress.getByName(host).getHostName
      if (!res.contains(hostName)) {
        res(hostName) = new ArrayBuffer[String]()
      }
      res(hostName).+=(executorId)
    }
    res
  }

  def executorDatMapToId2Host(
      executorDataMap: mutable.HashMap[String, ExecutorData]
  ): mutable.HashMap[String, String] = {
    val res = new mutable.HashMap[String, String]()
    for (tuple <- executorDataMap) {
      val executorId = tuple._1
      val host       = tuple._2.executorHost
      log.info(s"executor id ${executorId}, host ${host}")
      //here we got ip, cast to hostname
      val hostName = InetAddress.getByName(host).getHostName
      res.+=((executorId, hostName))
    }
    res
  }
}
