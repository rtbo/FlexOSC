package rtbo.flexosc.viewmodel

fun populateDummyModel(model: SurfaceModel) {

    val play = LedButtonModel(model)
    play.sendAddress = "/transport_play"
    play.rcvAddress = "/transport_play"
    play.ledIcon = PLAY_ICON
    play.ledColor = 0xff00ff00.toInt()

    val stop = ButtonModel(model)
    stop.sendAddress = "/transport_stop"
    stop.position = Position(1, 0)
    stop.icon = STOP_ICON

    val rec = LedButtonModel(model)
    rec.sendAddress = "/rec_enable_toggle"
    rec.rcvAddress = "/rec_enable_toggle"
    rec.position = Position(2, 0)
    rec.ledIcon = REC_ICON
    rec.ledColor = 0xffff0000.toInt()

    val stopTrash = ButtonModel(model)
    stopTrash.sendAddress = "/stop_forget"
    stopTrash.icon = STOP_TRASH_ICON
    stopTrash.position = Position(3, 0)

    val addMark = ButtonModel(model)
    addMark.sendAddress = "/add_marker"
    addMark.icon = ADD_ICON
    addMark.position = Position(4, 0)

    val remMark = ButtonModel(model)
    remMark.sendAddress = "/remove_marker"
    remMark.icon = REM_ICON
    remMark.position = Position(5, 0)

    val startMark = ButtonModel(model)
    startMark.sendAddress = "/goto_start"
    startMark.icon = START_ICON
    startMark.position = Position(0, 1)

    val prevMark = ButtonModel(model)
    prevMark.sendAddress = "/prev_marker"
    prevMark.icon = PREV_ICON
    prevMark.position = Position(1, 1)

    val nextMark = ButtonModel(model)
    nextMark.sendAddress = "/next_marker"
    nextMark.icon = NEXT_ICON
    nextMark.position = Position(2, 1)

    val endMark = ButtonModel(model)
    endMark.sendAddress = "/goto_end"
    endMark.icon = END_ICON
    endMark.position = Position(3, 1)

    model.addControl(play)
    model.addControl(stop)
    model.addControl(rec)
    model.addControl(stopTrash)
    model.addControl(addMark)
    model.addControl(remMark)
    model.addControl(startMark)
    model.addControl(endMark)
    model.addControl(prevMark)
    model.addControl(nextMark)
}
