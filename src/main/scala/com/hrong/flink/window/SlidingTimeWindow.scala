package com.hrong.flink.window

import java.text.SimpleDateFormat
import java.util.Date

import com.hrong.flink.model.Income
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.time.Time

/**
  * 有重叠数据，每5秒钟统计一次前10秒的数据
  * 输入：1:1
  *      1:2
  *      1:1
  *      1:2
  * 输出：
  *      receive : 1:1
  *      2019-05-16 11:22:42 compute result::6> Income(1,1.0)
  *      receive : 1:2
  *      2019-05-16 11:22:42 compute result::6> Income(1,3.0)
  *      receive : 1:1
  *      2019-05-16 11:22:42 compute result::6> Income(1,3.0)
  *      receive : 1:2
  *      2019-05-16 11:22:42 compute result::6> Income(1,3.0)
  *      2019-05-16 11:22:42 compute result::6> Income(1,2.0)
  *
  */
object SlidingTimeWindow {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val data: DataStream[String] = env.socketTextStream("s102", 9999)
    val incomeData = data.map(item => {
      println(s"receive : $item")
      try {
        val storeId_money = item.split(":")
        val storeId = storeId_money(0).toInt
        val money = storeId_money(1).toDouble
        Income(storeId, money)
      } catch {
        case _: Exception =>
          println(s"输入数据${item}不满足要求")
          Income(0, 0)
      }
    })
    val moneySum = incomeData.keyBy("storeId")
      .timeWindow(Time.seconds(10L), Time.seconds(5L))
      .sum("money")
    moneySum.print(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date) + " compute result:")
    env.execute(this.getClass.getName)
  }
}
