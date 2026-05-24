import request from '@/utils/request'

export function getStatistics() {
  return request({
    url: '/admin/statistics/inventory',
    method: 'get'
  })
}

export function getPopularStats() {
  return request({
    url: '/admin/statistics/popular',
    method: 'get'
  })
}

export function getBorrowTrend(days) {
  return request({
    url: '/admin/statistics/trend',
    method: 'get',
    params: { days }
  })
}

export function exportReport(type, startDate, endDate) {
  return request({
    url: '/admin/report/export',
    method: 'get',
    params: { type, startDate, endDate },
    responseType: 'blob'
  })
}