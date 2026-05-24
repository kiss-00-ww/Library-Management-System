<template>
  <div class="system-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统参数配置</span>
          <el-button type="primary" @click="handleSave" :loading="saving">保存修改</el-button>
        </div>
      </template>

      <template v-if="configLoading">
        <el-skeleton :rows="8" animated />
      </template>
      <template v-else>
      <el-table :data="configs" border stripe style="width: 100%">
        <el-table-column prop="configKey" label="配置项" width="240">
          <template #default="{ row }">
            <span class="config-key">{{ row.configKey }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" width="200" />
        <el-table-column prop="configValue" label="配置值" width="180">
          <template #default="{ row }">
            <el-input v-model="row.configValue" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="默认值参考" min-width="180">
          <template #default="{ row }">
            <span class="default-value">{{ getDefaultHint(row.configKey) }}</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无配置数据" :image-size="80" />
        </template>
      </el-table>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const configs = ref([])
const saving = ref(false)
const configLoading = ref(false)

const defaultHints = {
  borrow_days_student: '学生借阅天数，建议 30',
  borrow_days_teacher: '教师借阅天数，建议 60',
  max_borrow_count_student: '学生最大借阅数量，建议 5',
  max_borrow_count_teacher: '教师最大借阅数量，建议 10',
  renew_max_times: '最大续借次数，建议 1',
  renew_days: '续借天数，建议 30',
  renew_window_days: '到期前N天内可续借，建议 7',
  fine_rate_per_day: '每日逾期罚款(元)，建议 0.1',
  reserve_keep_days: '预约保留天数，建议 7'
}

const getDefaultHint = (key) => {
  return defaultHints[key] || ''
}

const fetchConfigs = async () => {
  configLoading.value = true
  try {
    const res = await request.get('/admin/config/list')
    configs.value = res.data
  } catch (e) {
    ElMessage.error('获取配置失败')
  } finally {
    configLoading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const configMap = {}
    configs.value.forEach(item => {
      configMap[item.configKey] = item.configValue
    })
    await request.put('/admin/config/update', configMap)
    ElMessage.success('配置保存成功，已实时生效')
  } catch (e) {
    ElMessage.error('保存配置失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchConfigs()
})
</script>

<style scoped>
.system-config {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 18px;
  font-weight: bold;
}

.config-key {
  font-family: monospace;
  color: #409eff;
}

.default-value {
  color: #909399;
  font-size: 13px;
}
</style>
