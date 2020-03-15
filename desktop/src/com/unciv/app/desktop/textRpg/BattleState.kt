package com.unciv.app.desktop.textRpg

import com.unciv.app.desktop.*


class BattleState(val player: Player, val enemy: Combatant): State() {
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
            println("  --------   ")
            if (!player.encounters.contains(Encounter.reset)) {
                println("You open your eyes, and and see a familiar ceiling.")
                println("You're back in your room. Were you saved? How long have you been here?")
                println("You go downstairs and ask your mother what happened, who attacked us? Is everyone okay?")
                println("She looks at you strangely, tells you you must have had an extremely odd dream, ")
                println("  and that you should get your things ready to head back to the Academy - you wouldn't want to miss the first day of school!")
                player.encounters += Encounter.reset
            } else {
                println("You wake up at home again.")
            }

            player.health = player.maxHealth
            return PreparationState(player)
        }
        return this
    }

    private fun youWin() = enemy.health <= 0
    private fun enemyWins() = player.health <= 0
}