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
import com.unciv.app.desktop.textRpg.*
import com.unciv.models.translations.tr
import java.io.File
import kotlin.concurrent.thread
import kotlin.system.exitProcess


class RmGameInfo{
    val unit: Combatant
    var day = 1
    fun passDay(){
        day++
        println("Day $day")
        println("You are ${unit.hunger}")
    }

    init {
        unit = Combatant("Goblin")
        unit.abilities += MonsterGenerator.run.copy()
        unit.abilities += Ability("Punch", arrayListOf("Strength", "Body"), listOf("Damage=2", "Energy=1"))
        unit.abilities += Ability("Spear Thrust", arrayListOf("Strength", "Spear", "Accuracy"),
                listOf("Damage=10", "Energy=10", "Requires=Spear"))
        unit.abilities += Ability("Spear Throw", arrayListOf("Strength", "Spear", "Accuracy","Ranged"),
                listOf("Damage=15", "Energy=10", "Requires=Spear","Ranged","LoseRequired","CauseStatus=Burdened"))
                .apply { experience=100 }
        unit.abilities += Ability("Stab", arrayListOf("Strength", "Dagger"), listOf("Damage=4", "Energy=2", "Requires=Dagger"))
                .apply { experience=10 }
        unit.items += Item("Pathetic wooden spear", "Spear", "Equip=Hands").apply { isEquipped=true }
        println("You wake up hungry again.")
        println("You are a Goblin, one of many in this cave.")
        println("But something feels different, now - you're confident that life is changing for the better.")
    }
}

internal object DesktopLauncher {

    @JvmStatic
    fun main(arg: Array<String>) {

        if(true) {
            val rmGameInfo = RmGameInfo()

            var state: State = RmBaseState()
            while (state !is VictoryState && state !is DefeatState) {
                state = state.nextState(rmGameInfo)
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
