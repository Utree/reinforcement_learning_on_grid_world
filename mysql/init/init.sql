USE grid_world;

-- Table1: agents
CREATE TABLE `agents` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '場所のプライマリーキー',
  `grid_width` INT NOT NULL COMMENT 'グリッドの横幅',
  `goal_x` INT NOT NULL COMMENT 'ゴールのx座標',
  `goal_y` INT NOT NULL COMMENT 'ゴールのy座標',
  `learning_counter` INT NOT NULL COMMENT '学習回数',
  `step_limit` INT NOT NULL COMMENT 'ステップ回数の上限',
  `is_succeed` INT NOT NULL COMMENT 'ゴール到達のフラグ true:1, false:0',
  `is_learning` INT NOT NULL COMMENT '学習と評価のフラグ 評価:1, 学習:0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='エージェント';

-- Table2: movement_log
CREATE TABLE `movement_log` (
  `agent_id` INT NOT NULL COMMENT '紐付けるエージェント',
  `x` INT NOT NULL COMMENT 'ゴールのx座標',
  `y` INT NOT NULL COMMENT 'ゴールのy座標',
  `step_counter` INT NOT NULL COMMENT 'ステップ回数 0ベース',
  CONSTRAINT `fk_agent_id1` FOREIGN KEY(`agent_id`) REFERENCES agents(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='エージェントの移動ログ';

-- Table3: grid_world
CREATE TABLE `grid_world` (
  `agent_id` INT NOT NULL COMMENT '紐付けるエージェント',
  `x` INT NOT NULL COMMENT 'ゴールのx座標',
  `y` INT NOT NULL COMMENT 'ゴールのy座標',
  `left_arrow` INT NOT NULL COMMENT '左方向の矢印',
  `up_arrow` INT NOT NULL COMMENT '上方向の矢印',
  `down_arrow` INT NOT NULL COMMENT '下方向の矢印',
  `right_arrow` INT NOT NULL COMMENT '右方向の矢印',
  CONSTRAINT `fk_agent_id2` FOREIGN KEY(`agent_id`) REFERENCES agents(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='グリッドワールドの情報';
