import axios from '../utils/axios'

export const recipeApi = {
  getAll(params) {
    return axios.get('/recipes', { params })
  },

  getAllList() {
    return axios.get('/recipes/list')
  },

  getById(id) {
    return axios.get(`/recipes/${id}`)
  },

  create(data) {
    return axios.post('/recipes', data)
  },

  update(id, data) {
    return axios.put(`/recipes/${id}`, data)
  },

  delete(id) {
    return axios.delete(`/recipes/${id}`)
  },

  getCuisines() {
    return axios.get('/recipes/cuisines')
  },

  getDifficulties() {
    return axios.get('/recipes/difficulties')
  },

  getRandom() {
    return axios.get('/recipes/random')
  },

  getCount() {
    return axios.get('/recipes/count')
  }
}

export const shoppingApi = {
  getAll() {
    return axios.get('/shopping')
  },

  getUnpurchased() {
    return axios.get('/shopping/unpurchased')
  },

  getPurchased() {
    return axios.get('/shopping/purchased')
  },

  getById(id) {
    return axios.get(`/shopping/${id}`)
  },

  create(data) {
    return axios.post('/shopping', data)
  },

  addFromRecipe(recipeId) {
    return axios.post(`/shopping/from-recipe/${recipeId}`)
  },

  update(id, data) {
    return axios.put(`/shopping/${id}`, data)
  },

  togglePurchased(id) {
    return axios.put(`/shopping/${id}/toggle`)
  },

  delete(id) {
    return axios.delete(`/shopping/${id}`)
  },

  deletePurchased() {
    return axios.delete('/shopping/purchased')
  },

  clearAll() {
    return axios.delete('/shopping/all')
  },

  getStats() {
    return axios.get('/shopping/stats')
  }
}

export const fileApi = {
  upload(file) {
    const formData = new FormData()
    formData.append('file', file)
    return axios.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  uploadMultiple(files) {
    const formData = new FormData()
    for (let i = 0; i < files.length; i++) {
      formData.append('files', files[i])
    }
    return axios.post('/files/upload-multiple', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  delete(url) {
    return axios.delete('/files', { params: { url } })
  }
}

export const exportApi = {
  exportPdf(id) {
    return axios.get(`/export/recipe/${id}/pdf`, {
      responseType: 'blob'
    }).then(response => {
      const blob = new Blob([response], { type: 'application/pdf' })
      return blob
    })
  },

  previewPdf(id) {
    return axios.get(`/export/recipe/${id}/preview`, {
      responseType: 'blob'
    }).then(response => {
      const blob = new Blob([response], { type: 'application/pdf' })
      return URL.createObjectURL(blob)
    })
  },

  downloadPdf(id, filename) {
    return this.exportPdf(id).then(blob => {
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = filename || 'recipe.pdf'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
    })
  }
}
