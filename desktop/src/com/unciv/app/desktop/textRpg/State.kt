package com.unciv.app.desktop.textRpg

import com.unciv.app.desktop.RmGameInfo

class Action(val name:String, val action:()->Unit)
abstract class State {
    abstract fun nextState(rmGameInfo: RmGameInfo): State

    fun chooseAndActivateAction(actions:List<Action>){
//        if(actions.size==1){
//            actions.first().action()
//            return
//        }
        val chosenAction = chooseAction(actions.map { it.name })
        actions[chosenAction].action()
    }

    fun chooseAction(actions: List<String>): Int {
        for ((i, action) in actions.withIndex()) {
            val placement = i+1
            println("$placement - $action")
        }
        while (true) {
            val read = readLine()
            if (read == null) continue
            try {
                val chosenIndex = read.toInt() -1
                if (chosenIndex !in actions.indices) throw Exception()
                return chosenIndex
            } catch (ex: java.lang.Exception) {
                println("Not a valid choice!")
            }
        }
    }
}

class VictoryState(): State() {
    override fun nextState(rmGameInfo: RmGameInfo): State {
        println("You beat the entire game!")
        return this
    }
}
class DefeatState(): State() {
    override fun nextState(rmGameInfo: RmGameInfo): State {
        println("You Lose, loser!")
        return this
    }
}