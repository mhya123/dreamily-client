package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.player.Bl1nk
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.*
import net.minecraft.network.play.server.SPacketEntity.S15PacketEntityRelMove
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

@ModuleInfo(name = "GrimVelocity", description = "Grim", category = ModuleCategory.COMBAT)
class GrimVelocity : Module() {
    private val cancelPacketValue = IntegerValue("GroundTicks",6,0,100)
    private val AirCancelPacketValue = IntegerValue("AirTicks",6,0,100)
    private val cancelS12PacketValue = BoolValue("NoS12",true)
    private val ModeValue = ListValue("CancelPacket", arrayOf("S32", "C0f","none"), "S32")
    private val C0fResend = BoolValue("C0fResend",false)
    private val S32Test = BoolValue("S32Test",false)
    val DelayClientPacket = BoolValue("DelayClintPacket",false)
    private val ServerPacketTest = BoolValue("ServerPacketTest",false)
    private val NoMoveFix = BoolValue("NoMoveFix",false)
    private val OnlyMove = BoolValue("OnlyMove",false)
    private val OnlyGround = BoolValue("OnlyGround",false)
    private val AutoDisableMode = ListValue("AutoDisableMode",arrayOf("Safe", "Silent"),"slient")
   private val AutoSilent = IntegerValue("AutoSilentTicks",8,0,10)
    var cancelPackets = 0
    private var resetPersec = 8
    private var updates = 0
    private var S08 = 0
    private val C0fPacket = LinkedBlockingQueue<Packet<*>>()
    private val S32Packet = LinkedList<Packet<INetHandlerPlayClient>>()
    private val SPacket = LinkedList<Packet<INetHandlerPlayClient>>()
    private val debugValue = BoolValue("debug",false)
    private val bl1nk = LiquidBounce.moduleManager.getModule(Bl1nk::class.java)
    fun debug(s: String) {
        if (debugValue.get())
            ClientUtils.displayChatMessage(s)
    }
    override fun onEnable() {
        if(DelayClientPacket.get()){
            if(bl1nk!!.state == true) {
                bl1nk!!.state = false
            }
        }
         cancelPackets=0

        S32Packet.clear()
        C0fPacket.clear()
    }
    override fun onDisable(){
        if(DelayClientPacket.get()){
            if(bl1nk!!.state == true) {
                bl1nk!!.state = false
            }
        }
        S32Packet.clear()
        C0fPacket.clear()
    }
    @EventTarget
    fun onWorld(event:WorldEvent){

        S32Packet.clear()
        C0fPacket.clear()
    }
    @EventTarget
    fun onPacket(event: PacketEvent){
        if((OnlyMove.get()&&!MovementUtils.isMoving)||(OnlyGround.get()&&!mc.thePlayer!!.onGround)){return}
        val packet = event.packet.unwrap()
        if(S08>0){
            S08--
            debug("Off $S08")
            return
        }
        if(packet is SPacketPlayerPosLook){
            if(AutoDisableMode.get().equals("silent", ignoreCase = true)){
                S08 = AutoSilent.get()
            }
            if(AutoDisableMode.get().equals("safe", ignoreCase = true)){
              LiquidBounce.moduleManager.get(GrimVelocity::class.java)!!.state = false
            }
        }
        if (packet is SPacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                return
            }
            if(NoMoveFix.get()&&!MovementUtils.isMoving&& mc.thePlayer!!.onGround){
                if(cancelS12PacketValue.get()) {
                    packet.motionX = 0
                    packet.motionY = 0
                    packet.motionZ = 0
                }
            }else{
                if(cancelS12PacketValue.get())
                event.cancelEvent()
            }

            cancelPackets = if(mc.thePlayer!!.onGround) cancelPacketValue.get() else AirCancelPacketValue.get()

        }
        if (( ModeValue.get().equals("c0f", ignoreCase = true)||(packet is SPacketConfirmTransaction && ModeValue.get().equals("s32", ignoreCase = true)) ||(packet is CPacketConfirmTransaction && ModeValue.get().equals("c0f", ignoreCase = true)) )&& cancelPackets > 0){
            if(C0fResend.get()&&ModeValue.get().equals("c0f", ignoreCase = true)){
                C0fPacket.add(packet)
            }
            if(S32Test.get() && ModeValue.get().equals("s32", ignoreCase = true)){
                S32Packet.add(packet as Packet<INetHandlerPlayClient>)
            }
            if(ModeValue.get().equals("s32", ignoreCase = true)){
                debug("S32")
            }else{
                debug("C0f")
            }
            event.cancelEvent()
            cancelPackets--
        }
        if(cancelPackets > 0){
            if(DelayClientPacket.get() && (MovementUtils.isMoving||!OnlyMove.get())){
                if(bl1nk!!.state == false) {
                    bl1nk!!.state = true
                }
            }
            if(ServerPacketTest.get()){
                if(packet is SPacketEntityVelocity || packet is SPacketEntity || packet is SPacketSpawnPlayer || packet is SPacketEntityTeleport || packet is S15PacketEntityRelMove){
                    if(packet is SPacketEntityVelocity){
                        if ((mc.theWorld?.getEntityByID(packet.entityID) ?: return) == mc.thePlayer) {
                            return
                        }
                    }
                    SPacket.add(packet as Packet<INetHandlerPlayClient>)
                    event.cancelEvent()
                }
            }
        }
    }
    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if((OnlyMove.get()&&!MovementUtils.isMoving)||(OnlyGround.get()&&!mc.thePlayer!!.onGround)){return}
        updates++
        if (resetPersec > 0) {
            if (updates >= 0 || updates >= resetPersec) {
                updates = 0
                if (cancelPackets > 0){
                    cancelPackets--
                }
            }
        }


        if(cancelPackets == 0){
            if(DelayClientPacket.get()){
                if(bl1nk!!.state == true) {
                    bl1nk!!.state = false
                }
            }
            if(!C0fPacket.isEmpty()){
                while (!C0fPacket.isEmpty()) {
                    mc2.connection!!.networkManager.sendPacket(C0fPacket.take())
                    C0fPacket.clear()
                    debug("C0fResend")
                }
            }
            if(!S32Packet.isEmpty()){
                while (S32Packet.size > 0) {
                    S32Packet.poll()?.processPacket(mc2.connection)
                    S32Packet.clear()
                    debug("S32Test")
                }
            }
            if(!SPacket.isEmpty()){
                while (SPacket.size > 0) {
                    SPacket.poll()?.processPacket(mc2.connection)
                    SPacket.clear()
                    debug("STest")
                }
            }
        }
    }

}