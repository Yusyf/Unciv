package com.unciv.app.desktop.textRpg

import com.unciv.app.desktop.RmGameInfo

class RmBaseState: State() {

    override fun nextState(gameInfo: RmGameInfo): State {
        var nextState: State = this
        val unit = gameInfo.unit

        if (unit.health < unit.maxHealth) {
            println("You are injured. Health: " + unit.health)
            println("You are tired. Energy: " + unit.energy)
        }
        val huntAction = Action("Hunt") {
            val enemy = listOf(MonsterGenerator.getHornRabbit(), MonsterGenerator.getWolf()).random()
            println("You encounter a ${enemy.name}!")
            nextState = RmBattleState(unit, enemy)
        }

        val restAction = Action("Rest (next day)") {
            if(unit.hunger==Hunger.Starving){
                println("Your fellow goblins take pity on you and give you something to eat.")
                println("You're still starving, but at least you won't die quite yet.")
                unit.decreaseHunger()
            }
            val healthGained = when(unit.hunger){
                Hunger.Bloated,Hunger.Full,Hunger.Sated -> 50
                Hunger.Hungry -> 30
                Hunger.Starving -> 10
            }
            unit.healBy(healthGained)
            unit.energy = 100
            println("You rest and recover some health (health: ${unit.health})")
            unit.increaseHunger()
            gameInfo.passDay()
        }

        val trainAction = Action("Train") {
            val trainActions = unit.abilities
                    .filter { unit.canUse(it) }
                    .map {
                        Action(it.name+" (Expertise: ${it.getAbilityLevel()})") {
                            it.experience += unit.energy
                            unit.energy = 0
                            println("You train until you're out of energy")
                            restAction.action()
                        }
                    } + Action("Exit") {}
            chooseAndActivateAction(trainActions)
        }

        val itemAction = Action("Items") {
            val itemActions = unit.items.sortedByDescending { it.isEquipped }.map {
                var title = it.name
                if (it.isEquipped) title += " (equipped)"
                Action(title) {
                    val actions = arrayListOf<Action>()
                    if (it.isEquipped) actions += Action("Unequip") { it.isEquipped = false }
                    if (!it.isEquipped && it.isEquippable()) actions += Action("Equip") {
                        val equipSlot = it.equipSlot()
                        for (item in unit.items.filter { it.equipSlot() == equipSlot })
                            item.isEquipped = false
                        it.isEquipped = true
                    }
                    if("Food" in it.parameters) actions += Action("Eat"){
                        unit.decreaseHunger()
                        unit.items.remove(it)
                    }
                    actions += Action("Exit") {}
                    chooseAndActivateAction(actions)
                }
            }
            chooseAndActivateAction(itemActions)
        }.takeIf { unit.items.any() }

        chooseAndActivateAction(listOf(huntAction, restAction, trainAction,itemAction).filterNotNull())
        return nextState
    }
}