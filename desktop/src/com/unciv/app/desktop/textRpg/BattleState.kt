package com.unciv.app.desktop.textRpg

import com.unciv.app.desktop.Combatant
import com.unciv.app.desktop.State
import com.unciv.app.desktop.VictoryState

class BattleState(val player: Combatant, val enemy: Combatant): State() {
    override fun nextState(): State {
        println("Your health: ${player.health}")

        val playerAttacks = player.abilities.filter { it.isAttack() }
        val chosenAttackIndex = chooseAction(playerAttacks.map { it.name })
        val chosenAttack = playerAttacks[chosenAttackIndex]
        val playerAttackDamage = chosenAttack.calculateDamage(player)
        enemy.health -= playerAttackDamage
        println("You use ${chosenAttack.name} for $playerAttackDamage damage! ${enemy.name} has ${enemy.health} health!")
        if (youWin()) {
            println("You defeated the ${enemy.name}!")
            return VictoryState()
        }

        val enemyAttack = enemy.abilities.filter { it.isAttack() }.random()
        val enemyAttackDamage = enemyAttack.calculateDamage(enemy)
        println("${enemy.name} used ${enemyAttack.name} for $enemyAttackDamage damage!")
        player.health -= enemyAttackDamage
        if (enemyWins()) {
            println("You died a miserable death!")
            println("When you wake up, you are back at the preparation stage!")
            player.health=player.maxHealth
            return PreparationState(player)
        }
        return this
    }

    private fun youWin() = enemy.health <= 0
    private fun enemyWins() = player.health <= 0
}