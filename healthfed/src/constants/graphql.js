import gql from 'graphql-tag'


export const SAVE_OBJ_DETECT_IMAGE = gql`
mutation SaveObjDetectImage($id: String!, $project: String!, 
                            $annotations: [AnnotationInput]) {
    saveObjDetectImage(id: $id, project: $project, 
                       annotations: $annotations) {
        id
    }
}
`

export const ALL_POST = gql`
mutation AllPost($name: String!, 
                 $description: String!, 
                 $tissue: String!,
                 $dataset: String!) {


    allPost(name: $name, 
            description: $description, 
            tissue: $tissue,
            dataset: $dataset) { name}
}
`