import java.util.*

/**
 *  gridのcell
 *
 *  @param up
 *  上向き矢印の長さ(移動不可の場合は-1)
 *
 *  @param down
 *  下向き矢印の長さ(移動不可の場合は-1)
 *
 *  @param right
 *  右向き矢印の長さ(移動不可の場合は-1)
 *
 *  @param left
 *  左向き矢印の長さ(移動不可の場合は-1)
 */
data class Cell(
    var left: Int,
    var up: Int,
    var down: Int,
    var right: Int
)

/**
 *  座標
 *
 *  @property x
 *  x座標
 *
 *  @property y
 *  y座標
 */
data class Position(var x: Int, var y: Int)

/**
 * グリッドワールドのマップ
 * _|0_x___
 * 0|
 * y|
 *  |
 *
 * @property cells
 * グリッドワールドのマップチップ
 */
data class World(var cells: MutableMap<Position, Cell>)
// グリッドワールドを初期化
fun MutableMap<Position, Cell>.init(index: Int) {
    for(mapInitI in 0 until index) {
        for(mapInitJ in 0 until index) {
            // up
            val up: Int = if (mapInitI > 0) 0 else -1
            // down
            val down: Int = if (mapInitI < index - 1) 0 else -1
            // right
            val right: Int = if (mapInitJ < index - 1) 0 else -1
            // left
            val left: Int = if (mapInitJ > 0) 0 else -1

            this[Position(mapInitJ, mapInitI)] = Cell(left, up, down, right)
        }
    }
}

/**
 * エージェント
 *
 * @property grid
 * グリッドワールドの矢印長さの情報を持つ
 *
 * @property movementLog
 * 移動ログ
 */
data class Agent(val grid: World, var movementLog: MutableList<Position>) {
    private val rand = Random()

    /**
     * 次の移動先を決定する
     */
    fun move() {
        // 現在位置
        val currentPosition = movementLog.last()
        // 現在位置の上下左右の矢印の長さを取得
        val arrow: Cell = grid.cells[currentPosition]!!

        var awl = listOf(arrow.up, arrow.down, arrow.right, arrow.left)

        val y = if(isLearning) {
            0
        } else {
            awl.max()
        }

        val up = if(arrow.up >= y!!) arrow.up + 1 else 0
        val down = if(arrow.down >= y) arrow.down + 1 else 0
        val right = if(arrow.right >= y) arrow.right + 1 else 0
        val left = if(arrow.left >= y) arrow.left + 1 else 0

        awl = listOf(up, down, right, left)

        val x = rand.nextInt(up + down + right + left)

        // 現在位置を変更
        movementLog.add(
            if(0 <= x && x < awl[0]) {
                // UP
                Position(currentPosition.x, currentPosition.y-1)
            } else if(awl[0] <= x && x < awl[0] + awl[1]) {
                // DOWN
                Position(currentPosition.x, currentPosition.y+1)
            } else if(awl[0] + awl[1] <= x && x < awl[0] + awl[1] + awl[2]) {
                // RIGHT
                Position(currentPosition.x+1, currentPosition.y)
            } else {
                // LEFT
                Position(currentPosition.x-1, currentPosition.y)
            }
        )
    }

    /**
     * movementLogをリセット
     *
     * @param gridWidth グリッドワールドの幅
     *
     * @param goalPosition ゴールの位置
     *  ex)
     *   0  1  2  3
     *   4  5  6  7
     *   8  9 10 11
     *  12 13 14 15
     */
    fun resetPosition(gridWidth: Int, goalPosition: Int) {
        val nextAgentPositions = (0 until gridWidth*gridWidth).toMutableList()
        // ゴールとの重複をなくす
        nextAgentPositions.removeAt(goalPosition)
        // シャッフル
        nextAgentPositions.shuffle()
        // 再設定
        this.movementLog = mutableListOf(Position(nextAgentPositions[0]/gridWidth, nextAgentPositions[0]%gridWidth))
    }
}


/**
 * エントリーポイント
 */
fun main(args: Array<String>) {
    /**
     * グリッドワールドの広さ(横幅)
     *
     * 基本パラメータ: 5
     *
     * 考察(1-1)では2〜11までを実験する
     **/
    val gridsMax = 11
    val gridsMin = 2
    val gridsBasic = 5
    /**
     * ステップ(移動)回数の上限
     *
     * 基本パラメータ: 20
     *
     * 考察(2-4),(3-3)では25-15までを実験する
     **/
    val stepUpperLimits = 25
    val stepLowerLimits = 15
    val stepBasicLimits = 20
    /** 報酬 */
    val reward = 10
    /**
     * 学習の繰り返し回数
     *
     * 基本パラメータ: 20
     *
     * 考察(2-5),(3-4)では25-15までを実験する
     **/
    val repeatUpperNumberOfLearning = 25
    val repeatLowerNumberOfLearning = 15
    val repeatBasicNumberOfLearning = 20
    /** デバッグフラグ */
    val debug = true
    /**
     * agentの通し番号
     *
     * 注意: 保存するDBにはデータが入っていないことを確認
     */
    var agentCounter = 1
    /** Todo: DBからフィールド数を取得し、agentCounterを更新 */

    /** グリッドワールドの広さを定義 */
    for (gridWidth in gridsMin..gridsMax) {
        /** ゴール位置を決定 */
        val goalPositions = (0 until gridWidth * gridWidth).toMutableList()
        // 候補の中から無作為に抽出
        goalPositions.shuffle()
        for (goalPosition in goalPositions) {
            /** ゴールのPositionインスタンスを生成 */
            val goal = Position(goalPosition / gridWidth, goalPosition % gridWidth)

            /** グリッドワールドを生成 */
            // iは縦軸, jは横軸
            val initWorld = World(mutableMapOf())
            initWorld.cells.init(gridWidth)

            /** 考察時にエージェントの数が100になるように修正 */
            for(adjustment in 1..(when(gridWidth) {
                2 -> 34
                3 -> 13
                4 -> 7
                5 -> 5
                6,7 -> 3
                8,9,10 -> 2
                else -> 1
            })) {
                /** エージェントの初期位置を決定 */
                val agentPositions = (0 until gridWidth * gridWidth).toMutableList()
                // ゴールとの重複をなくす
                agentPositions.removeAt(goalPosition)
                // 候補の中から無作為に抽出
                agentPositions.shuffle()
                for (agentPosition in agentPositions) {
                    // エージェントのインスタンスを生成
                    val agent = Agent(initWorld, mutableListOf(Position(agentPosition / gridWidth, agentPosition % gridWidth)))

                    /** 学習回数を決定 */
                    for (repeatNum in 1..(if(gridWidth == gridsBasic && goalPosition == 0) {
                        repeatUpperNumberOfLearning
                    } else {
                        repeatBasicNumberOfLearning
                    })) {
                        /** ステップ回数を決定 */
                        for (step in stepLowerLimits..(if(gridWidth == gridsBasic && goalPosition == 0) {
                            stepUpperLimits
                        } else {
                            stepBasicLimits
                        })) {
                            /** Tの値を決定 */
                            for(paramT in memoryTLower..memoryTUpper) {
                                /**
                                 * バリデーション
                                 *
                                 * 1. グリッドワールドの幅が5
                                 * 2. ゴールの位置が(0, 0)
                                 * 3. 学習の繰り返し回数が20
                                 *
                                 *
                                 * のうち、いずれか2つに合致かつ、
                                 * Tが10の時、実行
                                 *
                                 * 1かつ２かつ３のときも実行
                                 **/
                                if((gridWidth == gridsBasic && goalPosition == 0 && paramT == memoryTBasic)
                                        || (gridWidth == gridsBasic && repeatNum == repeatBasicNumberOfLearning && paramT == memoryTBasic)
                                        || (goalPosition == 0 && repeatNum == repeatBasicNumberOfLearning && paramT == memoryTBasic)
                                        || (gridWidth == gridsBasic && goalPosition == 0 && repeatNum == repeatBasicNumberOfLearning)) {
                                    /** 学習開始 */
                                    var isSuccess = 0
                                    for(stp in 1..step) {
                                        // エージェントを移動
                                        agent.move()

                                        // ゴール時
                                        if (goal == agent.movementLog.last()) {
                                            // movementLogがTより小さかった時の挙動
                                            val pT = if(agent.movementLog.size-1 < paramT) {
                                                agent.movementLog.size-1
                                            } else {
                                                paramT
                                            }

                                            isSuccess = 1
                                            /**
                                             * arrowを計算 */
                                            for (movementLogIndex in agent.movementLog.size-2 downTo 0) {
                                                // ptは1からpTまでの値
                                                val pt = pT - movementLogIndex
                                                // 矢印の長さ
                                                val newLength = reward * (pT - pt + 1) / pT


                                                // movementLogの前後関係から移動方向を取得
                                                if (agent.movementLog[movementLogIndex].x < agent.movementLog[movementLogIndex + 1].x) {
                                                    // 右
                                                    agent.grid.cells[Position(agent.movementLog[movementLogIndex].x, agent.movementLog[movementLogIndex].y)]!!.right += newLength
                                                } else if (agent.movementLog[movementLogIndex].x > agent.movementLog[movementLogIndex + 1].x) {
                                                    // 左
                                                    agent.grid.cells[Position(agent.movementLog[movementLogIndex].x, agent.movementLog[movementLogIndex].y)]!!.left += newLength
                                                } else {
                                                    if (agent.movementLog[movementLogIndex].y > agent.movementLog[movementLogIndex + 1].y) {
                                                        // 上
                                                        agent.grid.cells[Position(agent.movementLog[movementLogIndex].x, agent.movementLog[movementLogIndex].y)]!!.up += newLength
                                                    } else {
                                                        // 下
                                                        agent.grid.cells[Position(agent.movementLog[movementLogIndex].x, agent.movementLog[movementLogIndex].y)]!!.down += newLength
                                                    }
                                                }
                                            }
                                        }
                                    }
                                /** DBに記録 */
                                // agentsを記録
                                SqlExecutor.executeSql(
                                        "INSERT INTO grid_world.agents(grid_width, goal_x, goal_y, learning_counter, step_limit, is_succeed, is_learning) " +
                                                "VALUES($gridWidth, ${goalPosition / gridWidth}, ${goalPosition % gridWidth}, $repeatNum, $step, $isSuccess, 1);"
                                )

                                // movement_logを記録
                                for(sqlMLog in 0 until agent.movementLog.size) {
                                    SqlExecutor.executeSql(
                                            "INSERT INTO grid_world.movement_log (agent_id, x, y, step_counter) " +
                                                    "VALUES($agentCounter, ${agent.movementLog[sqlMLog].x}, ${agent.movementLog[sqlMLog].y}, $sqlMLog);"
                                    )
                                }

                                // step_logsを記録
                                for(stepI in 0 until gridWidth) {
                                    for (stepJ in 0 until gridWidth) {
                                        SqlExecutor.executeSql(
                                                "INSERT INTO grid_world.grid_world (agent_id, x, y, left_arrow, up_arrow, down_arrow, right_arrow) " +
                                                        "VALUES($agentCounter, $stepJ, $stepI, ${agent.grid.cells[Position(stepJ, stepI)]!!.left}, ${agent.grid.cells[Position(stepJ, stepI)]!!.up}, ${agent.grid.cells[Position(stepJ, stepI)]!!.down}, ${agent.grid.cells[Position(stepJ, stepI)]!!.right});"
                                        )
                                    }
                                }
                                if(debug) {
                                    println("agent: $agentCounter is recorded.")
                                }

                                /** エージェントの初期位置を再設定 */
                                agent.resetPosition(gridWidth, goalPosition)
                                // エージェントの通し番号を更新
                                agentCounter++
                            }
                        }
                    }
                }
            }

        }
    }


    print("Hello Kotlin")
}
