import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { importOneBook } from '@/services/tankobon-importer'
import { BookEntity } from '@/types/tankobon-book'
import { ImportOneBook } from '@/types/tankobon-importer-source'
import { getRelationship } from '@/utils/api'

type ErrorResponse = TankobonApiError | Error

export default function useImportBookMutation() {
  const queryClient = useQueryClient()

  return useMutation<BookEntity, ErrorResponse, ImportOneBook>({
    mutationFn: importOneBook,
    onSuccess(book) {
      const library = getRelationship(book, 'LIBRARY')

      queryClient.invalidateQueries(['books', { libraryId: library?.id }])
    }
  })
}
