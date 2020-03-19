package com.unciv.app.desktop.textRpg

import com.unciv.app.desktop.RmGameInfo
import com.unciv.models.stats.Stat

class RmBaseState: State() {

    override fun nextState(gameInfo: RmGameInfo): State {
        var nextState: State = this
        val unit = gameInfo.unit

        if (unit.health < unit.maxHealth) {
            println("You are injured. Health: " + unit.health)
        }
        if(unit.energy<100) println("You are tired. Energy: " + unit.energy)
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
                    }
            chooseAndActivateAction(trainActions,true)
        }

        val talkAction = Action("Talk"){
            nextState=TalkState()
        }

        val itemAction = Action("Items") {
            nextState=ItemState()
        }.takeIf { unit.items.any() }

        chooseAndActivateAction(listOfNotNull(huntAction, restAction, trainAction, itemAction, talkAction))
        return nextState
    }
}

class ItemState():State(){
    override fun nextState(rmGameInfo: RmGameInfo): State {
        val unit = rmGameInfo.unit
        val itemActions = unit.items.groupBy { it }.values.sortedByDescending { it.first().isEquipped }.map {
            val firstItem = it.first()
            var title = firstItem.name
            if(it.size>1) title += " x"+it.size
            if (firstItem.isEquipped) title += " (equipped)"
            title += " (${firstItem.parameters.joinToString()})"
            Action(title) {
                val actions = arrayListOf<Action>()
                if (firstItem.isEquipped) actions += Action("Unequip") { firstItem.isEquipped = false }
                if (!firstItem.isEquipped && unit.canEquip(firstItem)) actions += Action("Equip") {
                    val equipSlot = firstItem.equipSlot()
                    for (item in unit.items.filter { it.equipSlot() == equipSlot })
                        item.isEquipped = false
                    firstItem.isEquipped = true
                }
                if ("Food" in firstItem.parameters) actions += Action("Eat") {
                    unit.decreaseHunger()
                    unit.items.remove(firstItem)
                }
                chooseAndActivateAction(actions, true)
            }
        }
        chooseAndActivateAction(itemActions,true)
        return RmBaseState()
    }
}

class TalkState():State(){
    override fun nextState(rmGameInfo: RmGameInfo): State {
        val unit = rmGameInfo.unit
        val talkActions = ArrayList<Action>()
        talkActions += Action("Talk to Leatherworker") {
            println("'I can make a bunch of things for ya - A tunic, some proper pants, a belt'")
            println("'But you need to bring me at least 5 skins that are the same.'")
            println("'I'll be taking some of those as my fee, see.'")
            println("'I can also add hard items to your leather clothing to make them tougher'")
            println("'So, you got anything for me?'")
            val skinsByName = unit.items.filter { it.parameters.contains("Skin") }
                    .groupBy { it.name }
            val mostSkinsOfSameType = skinsByName.values.map { it.size }.max()
            val skinActions = arrayListOf<Action>()
            if (mostSkinsOfSameType != null) {
                if (mostSkinsOfSameType >=  10)
                    skinActions += Action("I'd like a tunic (10 skins)") {
                        val skinsWithOver10 =skinsByName.values.filter { it.size>=10 }
                        if(skinsWithOver10.size==1){
                            val chosenSkins = skinsWithOver10.first()
                            val newTunic = ItemGenerator.getLeatherTunic(chosenSkins.first())
                            unit.items.removeAll(chosenSkins.take(10))
                            unit.addItem(newTunic)
                        }
                        else TODO()
                    }
            }
            skinActions += Action("Nothing right now"){}
            chooseAndActivateAction(skinActions)
        }
        chooseAndActivateAction(talkActions,true)
        return RmBaseState()
    }

}