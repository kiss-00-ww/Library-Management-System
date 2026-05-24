import request from '@/utils/request'

export function getUserInfo() {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}

export function updateUser(data) {
  return request({
    url: `/user/profile`,
    method: 'put',
    data
  })
}

export function getUsers(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

export function updateUserStatus(id, status) {
  return request({
    url: `/admin/users/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function updateUserRole(id, role) {
  return request({
    url: `/admin/users/${id}/role`,
    method: 'put',
    params: { role }
  })
}

export function deleteUser(id) {
  return request({
    url: `/admin/users/${id}`,
    method: 'delete'
  })
}

export function uploadAvatar(data) {
  return request({
    url: '/user/avatar',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}
