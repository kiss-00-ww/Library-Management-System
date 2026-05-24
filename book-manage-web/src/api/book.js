import request from '@/utils/request'

export function getBooks(params) {
  return request({
    url: '/books',
    method: 'get',
    params
  })
}

export function getBookDetail(id) {
  return request({
    url: `/books/${id}`,
    method: 'get'
  })
}

export function addBook(data) {
  return request({
    url: '/admin/books',
    method: 'post',
    data
  })
}

export function updateBook(id, data) {
  return request({
    url: `/admin/books/${id}`,
    method: 'put',
    data
  })
}

export function deleteBook(id) {
  return request({
    url: `/admin/books/${id}`,
    method: 'delete'
  })
}

export function toggleBookStatus(id) {
  return request({
    url: `/admin/books/${id}/status`,
    method: 'put'
  })
}

export function getCategories() {
  return request({
    url: '/categories',
    method: 'get'
  })
}

export function borrowBook(bookId) {
  return request({
    url: `/borrow/${bookId}`,
    method: 'post'
  })
}

export function returnBook(recordId) {
  return request({
    url: `/return/${recordId}`,
    method: 'post'
  })
}

export function renewBook(recordId) {
  return request({
    url: `/renew/${recordId}`,
    method: 'post'
  })
}

export function getMyBorrows(params) {
  return request({
    url: '/borrow/my',
    method: 'get',
    params
  })
}

export function getMyBorrowCounts() {
  return request({
    url: '/borrow/my/counts',
    method: 'get'
  })
}

export function checkBorrowStatus(bookId) {
  return request({
    url: '/borrow/check',
    method: 'get',
    params: { bookId }
  })
}

export function getMyBorrowedBookIds() {
  return request({
    url: '/borrow/my/bookIds',
    method: 'get'
  })
}

export function getAllBorrows(params) {
  return request({
    url: '/admin/borrows',
    method: 'get',
    params
  })
}

export function addCategory(data) {
  return request({
    url: '/admin/categories',
    method: 'post',
    data
  })
}

export function updateCategory(id, data) {
  return request({
    url: `/admin/categories/${id}`,
    method: 'put',
    data
  })
}

export function deleteCategory(id) {
  return request({
    url: `/admin/categories/${id}`,
    method: 'delete'
  })
}

export function createReservation(bookId) {
  return request({
    url: '/reader/reservation/create',
    method: 'post',
    params: { bookId }
  })
}

export function getMyReservations(params) {
  return request({
    url: '/reader/reservation/my',
    method: 'get',
    params
  })
}

export function borrowFromReservation(reservationId) {
  return request({
    url: `/reader/reservation/borrow/${reservationId}`,
    method: 'post'
  })
}

export function cancelReservation(reservationId) {
  return request({
    url: `/reader/reservation/cancel/${reservationId}`,
    method: 'post'
  })
}

export function downloadBookTemplate() {
  return request({
    url: '/admin/book/template',
    method: 'get',
    responseType: 'blob'
  })
}

export function importBooks(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/book/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
