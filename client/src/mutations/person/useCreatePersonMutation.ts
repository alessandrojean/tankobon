import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { addOnePerson } from '@/services/tankobon-people'
import type { PersonCreation, PersonEntity } from '@/types/tankobon-person'
import type { TankobonApiError } from '@/types/tankobon-response'

type ErrorResponse = TankobonApiError | Error

export default function useCreatePersonMutation() {
  const queryClient = useQueryClient()

  return useMutation<PersonEntity, ErrorResponse, PersonCreation>({
    mutationFn: addOnePerson,
    onSuccess(_, creation) {
      queryClient.invalidateQueries(['people', { libraryId: creation.library }])
    },
  })
}
