package com.unciv.app.desktop

import club.minnced.discord.rpc.DiscordEventHandlers
import club.minnced.discord.rpc.DiscordRPC
import club.minnced.discord.rpc.DiscordRichPresence
import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.tools.texturepacker.TexturePacker
import com.unciv.UncivGame
import com.unciv.models.translations.tr
import java.io.File
import kotlin.concurrent.thread
import kotlin.math.log10
import kotlin.system.exitProcess

data class Attack(val name:String, private var damage:Int, val experienceClasses: List<String>) {
    fun calculateDamage(experiences: List<Experience>): Int {
        var damageDouble = damage.toDouble()
        for (experience in experiences)
            if (experience.name in experienceClasses)
                damageDouble *= log10(experience.amount.toDouble())
        return damageDouble.toInt()
    }
}
class Experience(val name:String, val amount:Int)
class Combatant(val name:String, var health:Int, val attacks:List<Attack>, val experiences:List<Experience>)

val magicMissile = Attack("Magic Missile",10, listOf("Magic Missile"))

class BattleState(val player:Combatant, val enemy:Combatant):State() {
    override fun nextState(): State {
        println("Your health: ${player.health}")

        val chosenAttackIndex = chooseAction(player.attacks.map { it.name })
        val chosenAttack = player.attacks[chosenAttackIndex]
        val playerAttackDamage = chosenAttack.calculateDamage(player.experiences)
        println("You use ${chosenAttack.name} for $playerAttackDamage damage!")
        enemy.health -= playerAttackDamage
        if (youWin()) {
            println("You defeated the ${enemy.name}!")
            return VictoryState()
        }

        val enemyAttack = enemy.attacks.random()
        val enemyAttackDamage = enemyAttack.calculateDamage(enemy.experiences)
        println("${enemy.name} used ${enemyAttack.name} for $enemyAttackDamage damage!")
        player.health -= enemyAttackDamage
        if (enemyWins()) {
            println("You died a miserable death!")
            return DefeatState()
        }
        return this
    }

    private fun youWin() = enemy.health <= 0
    private fun enemyWins() = player.health <= 0
}

class PreparationState():State() {
    val player = Combatant("Player", 100, listOf(magicMissile), listOf(Experience("Magic Missile", 10)))
    var preparationTurns = 10

    override fun nextState(): State {
        if (preparationTurns > 0) {
            preparationTurns--
            return this
        } else {
            val enemy = Combatant("Cultist", 200, listOf(magicMissile), listOf(Experience("Magic Missile", 50)))
            return BattleState(player, enemy)
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

abstract class State {
    abstract fun nextState(): State

    fun chooseAction(actions: List<String>): Int {
        println("Choose an action:")
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

internal object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {

        if(true) {
            var state: State = PreparationState()
            while (state !is VictoryState && state !is DefeatState) {
                state = state.nextState()
            }
            return
        }

        packImages()

        val config = LwjglApplicationConfiguration()
        // Don't activate GL 3.0 because it causes problems for MacOS computers
        config.addIcon("ExtraImages/Icon.png", Files.FileType.Internal)
        config.title = "Unciv"
        config.useHDPI = true

        val versionFromJar = DesktopLauncher.javaClass.`package`.specificationVersion

        val game = UncivGame(if (versionFromJar != null) versionFromJar else "Desktop", null){exitProcess(0)}

        if(!RaspberryPiDetector.isRaspberryPi()) // No discord RPC for Raspberry Pi, see https://github.com/yairm210/Unciv/issues/1624
            tryActivateDiscord(game)

        LwjglApplication(game, config)
    }

    private fun packImages() {
        val startTime = System.currentTimeMillis()

        val settings = TexturePacker.Settings()
        // Apparently some chipsets, like NVIDIA Tegra 3 graphics chipset (used in Asus TF700T tablet),
        // don't support non-power-of-two texture sizes - kudos @yuroller!
        // https://github.com/yairm210/UnCiv/issues/1340
        settings.maxWidth = 2048
        settings.maxHeight = 2048
        settings.combineSubdirectories = true
        settings.pot = true
        settings.fast = true

        // This is so they don't look all pixelated
        settings.filterMag = Texture.TextureFilter.MipMapLinearLinear
        settings.filterMin = Texture.TextureFilter.MipMapLinearLinear

        if (File("../Images").exists()) // So we don't run this from within a fat JAR
            TexturePacker.process(settings, "../Images", ".", "game")

        // pack for mods as well
        val modDirectory = File("mods")
        if(modDirectory.exists()) {
            for (mod in modDirectory.listFiles()!!){
                TexturePacker.process(settings, mod.path + "/Images", mod.path, "game")
            }
        }

        val texturePackingTime = System.currentTimeMillis() - startTime
        println("Packing textures - "+texturePackingTime+"ms")
    }

    private fun tryActivateDiscord(game: UncivGame) {

        try {
            val handlers = DiscordEventHandlers()
            DiscordRPC.INSTANCE.Discord_Initialize("647066573147996161", handlers, true, null)

            Runtime.getRuntime().addShutdownHook(Thread { DiscordRPC.INSTANCE.Discord_Shutdown() })

            thread {
                while (true) {
                    try {
                        updateRpc(game)
                    }catch (ex:Exception){}
                    Thread.sleep(1000)
                }
            }
        } catch (ex: Exception) {
            print("Could not initialize Discord")
        }
    }

    fun updateRpc(game: UncivGame) {
        if(!game.isInitialized) return
        val presence = DiscordRichPresence()
        val currentPlayerCiv = game.gameInfo.getCurrentPlayerCivilization()
        presence.details=currentPlayerCiv.nation.getLeaderDisplayName().tr()
        presence.largeImageKey = "logo" // The actual image is uploaded to the discord app / applications webpage
        presence.largeImageText ="Turn".tr()+" " + currentPlayerCiv.gameInfo.turns
        DiscordRPC.INSTANCE.Discord_UpdatePresence(presence);
    }
}
