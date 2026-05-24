import request from '@/utils/request'

export function getUnreadCount() {
  return request({
    url: '/notification/unread-count',
    method: 'get'
  })
}

export function getNotifications(params) {
  return request({
    url: '/notification/list',
    method: 'get',
    params
  })
}

export function markAsRead(id) {
  return request({
    url: `/notification/read/${id}`,
    method: 'put'
  })
}

export function markAllAsRead() {
  return request({
    url: '/notification/read-all',
    method: 'put'
  })
}
