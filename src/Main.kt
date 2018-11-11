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
    for(i in 0 until index) {
        for (j in 0 until index) {
            // up
            val up: Int = if (i > 0) 0 else -1
            // down
            val down: Int = if (i < index - 1) 0 else -1
            // right
            val right: Int = if (j < index - 1) 0 else -1
            // left
            val left: Int = if (j > 0) 0 else -1

            this[Position(j, i)] = Cell(left, up, down, right)
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
        // 移動先を決定
        var destination: List<Int> = emptyList()
        // 確率を考慮してステップ先を選択
        for (k in 0..arrow.up)
            destination += 0
        for (k in 0..arrow.down)
            destination += 1
        for (k in 0..arrow.right)
            destination += 2
        for (k in 0..arrow.left)
            destination += 3

        // 現在位置を変更
        movementLog.add(
                when(destination[rand.nextInt(destination.size)]) {
                    // UP
                    0 -> Position(currentPosition.x, currentPosition.y-1)
                    // DOWN
                    1 -> Position(currentPosition.x, currentPosition.y+1)
                    // RIGHT
                    2 -> Position(currentPosition.x+1, currentPosition.y)
                    // LEFT
                    else -> Position(currentPosition.x-1, currentPosition.y)
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
        nextAgentPositions.drop(goalPosition)
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
    /** グリッドワールドの広さ */
    val grids = 11
    /** ステップ(移動)回数の上限 */
    val stepUpperLimits = 22
    /** 報酬 */
    val reward = 100
    /** 学習の繰り返し回数 */
    val repeatNumberOfLearning = 100
    val debug = false

    /** グリッドワールドの広さを定義 */
    for(gridWidth in 2 .. grids) {
        /** ゴール位置を決定 */
        for(goalPosition in 0 until gridWidth*gridWidth) {
            /** ゴールのPositionインスタンスを生成 */
            val goal = Position(goalPosition/gridWidth, goalPosition%gridWidth)

            /** グリッドワールドを生成 */
            // iは縦軸, jは横軸
            val initWorld = World(mutableMapOf())
            initWorld.cells.init(gridWidth)

            /** エージェントの初期位置を決定 */
            val agentPositions = (0 until gridWidth*gridWidth).toMutableList()
            // ゴールとの重複をなくす
            agentPositions.drop(goalPosition)
            for(agentPosition in agentPositions) {
                // エージェントのインスタンスを生成
                val agent = Agent(initWorld, mutableListOf(Position(agentPosition/gridWidth, agentPosition%gridWidth)))

                /** 学習回数を決定　*/
                for(repeatNum in 1..repeatNumberOfLearning) {
                    /** ステップ回数を決定 */
                    for(step in 1..stepUpperLimits) {
                        // エージェントを移動
                        agent.move()

                        // ゴール時
                        if(goal == agent.movementLog.last()) {
                            /** arrowを計算 */
                            for(i in 0 until agent.movementLog.size-1) {
                                val newLength = reward * (stepUpperLimits-step+1)/stepUpperLimits
                                // movementLogの前後関係から移動方向を取得
                                if(agent.movementLog[i].x < agent.movementLog[i+1].x) {
                                    // 右
                                    agent.grid.cells[Position(agent.movementLog[i+1].x, agent.movementLog[i+1].y)]!!.right += newLength
                                } else if(agent.movementLog[i].x > agent.movementLog[i+1].x) {
                                    // 左
                                    agent.grid.cells[Position(agent.movementLog[i+1].x, agent.movementLog[i+1].y)]!!.left += newLength
                                } else {
                                    if(agent.movementLog[i].y > agent.movementLog[i+1].y) {
                                        // 上
                                        agent.grid.cells[Position(agent.movementLog[i+1].x, agent.movementLog[i+1].y)]!!.up += newLength
                                    } else {
                                        // 下
                                        agent.grid.cells[Position(agent.movementLog[i+1].x, agent.movementLog[i+1].y)]!!.down += newLength
                                    }
                                }
                            }
                            break
                        }
                        // Debug
                        if(debug) {
                            println("gridWidth: $gridWidth, goalPosition: $goalPosition, agentPosition: $agentPosition, repeatNum: $repeatNum, stepUpperStepLimit: $step")
                        }
                    }


                    /** DBに記録 */

                    /** エージェントの初期位置を再設定 */
                    agent.resetPosition(gridWidth, goalPosition)
                }
            }
        }
    }


    print("Hello Kotlin")
}
