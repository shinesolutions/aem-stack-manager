### 1.0.1
* execute the start aem command even when an exception occurs during offline snapshot and compaction tasks

### 1.0.0
* remove compaction from offline snapshot task
* move author dispatcher and publish dispatcher to standby mode when stopping aem a part of offline snapshot task
* add source_stack_prefix to import package command
* create offline compaction snapshot task
* add checks to confirm the aem process has stopped before taking snapshot and performing compaction
* Messages can now be processed asynchronously

### 0.9.1
* add offline snapshot task


### 0.9.0
* Initial version
