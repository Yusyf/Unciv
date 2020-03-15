package com.unciv.app.desktop

class Action(val name:String, val action:()->Unit)
abstract class State {
    abstract fun nextState(): State

    fun chooseAndActivateAction(actions:List<Action>){
        if(actions.size==1){
            actions.first().action()
            return
        }
        val chosenAction = chooseAction(actions.map { it.name })
        actions[chosenAction].action()
    }

    fun chooseAction(actions: List<String>): Int {
        for ((i, action) in actions.withIndex()) {
            println("$i - $action")
        }
        while (true) {
            val read = readLine()
            if (read == null) continue
            try {
                val chosenIndex = read.toInt()
                if (chosenIndex !in actions.indices) throw Exception()
                return chosenIndex
            } catch (ex: java.lang.Exception) {
                println("Not a valid choice!")
            }
        }
    }
}

class VictoryState():State() {
    override fun nextState(): State {
        println("You beat the entire game!")
        return this
    }
}
class DefeatState():State() {
    override fun nextState(): State {
        println("You Lose, loser!")
        return this
    }
}