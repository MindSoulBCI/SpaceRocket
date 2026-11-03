package com.example.spacerocket.p9d574e9f.ui

import com.example.spacerocket.p9d574e9f.data.repository.SustainedAttentionRepository

object FlightViewModelFactory {
 val session:FlightViewModel by lazy { create() }
 fun create(): FlightViewModel {
  val source=SustainedAttentionRepository()
  return FlightViewModel(readInput=source::read,holdInput=source::setEngaged,advanceInput=source::advance,resetInput=source::reset)
 }
}
