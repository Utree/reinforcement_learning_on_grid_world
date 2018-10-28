USE grid_world;

-- Table1: positions
CREATE TABLE `positions` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '場所のプライマリーキー',
  `x` INT NOT NULL COMMENT 'x座標',
  `y` INT NOT NULL COMMENT 'y座標',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='位置';

-- Table2: cells セル内の矢印の長さを記録
CREATE TABLE `cells` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'プライマリーキー',
  `position` INT NOT NULL COMMENT '位置',
  `left` INT NOT NULL COMMENT '左向き矢印の長さ',
  `up` INT NOT NULL COMMENT '上向き矢印の長さ',
  `down` INT NOT NULL COMMENT '下向き矢印の長さ',
  `right` INT NOT NULL COMMENT '右向き矢印の長さ',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_position` FOREIGN KEY(`position`) REFERENCES positions(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='グリッドを構成するセル';

-- Table3: agents
CREATE TABLE `agents` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'エージェントのプライマリーキー',
  `learning_number` INT NOT NULL COMMENT '学習回数',
  `step_limit` INT NOT NULL COMMENT 'ステップ回数の上限',
  `cells` INT NOT NULL COMMENT 'グリッドが持つセル',
  CONSTRAINT `fk_cells` FOREIGN KEY(`cells`) REFERENCES cells(`id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='エージェントの記録';

-- Table4: step_logs
CREATE TABLE `step_logs` (
  `agent_id` INT NOT NULL COMMENT '紐付けるエージェント',
  `step_counter` INT NOT NULL COMMENT 'ステップ回数のカウンタ',
  `steps` INT NOT NULL COMMENT '位置',
  CONSTRAINT `fk_agent_id` FOREIGN KEY(`agent_id`) REFERENCES agents(`id`),
  CONSTRAINT `fk_steps` FOREIGN KEY(`steps`) REFERENCES positions(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='エージェントの移動ログ';
