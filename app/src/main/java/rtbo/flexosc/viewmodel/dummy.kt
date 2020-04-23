package rtbo.flexosc.viewmodel

import rtbo.flexosc.viewmodel.*

fun populateDummySurface(surface: ControlSurface) {
    val play = LedButtonControl(
        BoolReceivingCompoundCom(surface),
        Rect(0, 0, 1, 1)
    )
    play.com.address = "/transport_play"
    play.icon = Icon(
        IconId.PLAY,
        0xff00ff00.toInt()
    )

    val stop = ButtonControl(
        SendingCom(surface),
        Rect(1, 0, 1, 1)
    )
    stop.com.address = "/transport_stop"
    stop.icon =
        Icon(IconId.STOP)

    val rec = LedButtonControl(
        BoolReceivingCompoundCom(surface),
        Rect(2, 0, 1, 1)
    )
    rec.com.address = "/rec_enable_toggle"
    rec.icon = Icon(
        IconId.REC,
        0xffff0000.toInt()
    )

    val stopTrash = ButtonControl(
        SendingCom(surface), Rect(3, 0, 1, 1)
    )
    stopTrash.com.address = "/stop_forget"
    stopTrash.icon =
        Icon(IconId.STOP_TRASH)

    val addMark = ButtonControl(
        SendingCom(surface), Rect(4, 0, 1, 1)
    )
    addMark.com.address = "/add_marker"
    addMark.icon =
        Icon(IconId.ADD)

    val remMark = ButtonControl(
        SendingCom(surface), Rect(5, 0, 1, 1)
    )
    remMark.com.address = "/remove_marker"
    remMark.icon =
        Icon(IconId.REM)

    val startMark = ButtonControl(
        SendingCom(surface), Rect(0, 1, 1, 1)
    )
    startMark.com.address = "/goto_start"
    startMark.icon =
        Icon(IconId.START)

    val prevMark = ButtonControl(
        SendingCom(surface), Rect(1, 1, 1, 1)
    )
    prevMark.com.address = "/prev_marker"
    prevMark.icon =
        Icon(IconId.PREV)

    val nextMark = ButtonControl(
        SendingCom(surface), Rect(2, 1, 1, 1)
    )
    nextMark.com.address = "/next_marker"
    nextMark.icon =
        Icon(IconId.NEXT)

    val endMark = ButtonControl(
        SendingCom(surface), Rect(3, 1, 1, 1)
    )
    endMark.com.address = "/goto_end"
    endMark.icon =
        Icon(IconId.END)

    surface.addControl(play)
    surface.addControl(stop)
    surface.addControl(rec)
    surface.addControl(stopTrash)
    surface.addControl(addMark)
    surface.addControl(remMark)
    surface.addControl(startMark)
    surface.addControl(endMark)
    surface.addControl(prevMark)
    surface.addControl(nextMark)
}
