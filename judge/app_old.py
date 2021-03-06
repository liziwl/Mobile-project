from flask import Flask, request
from flask_restful import Api, Resource
import pandas as pd
from clf import One_auth
import os


app = Flask(__name__)
api = Api(app)

sample_response = {
    "passed": True,
    "requester": "ziqiang",
    "data": {
        "1": {
            "label": "ziqiang",
            "score": 0.8
        }
    }
}

BAD_REQUEST = ({'code': 400, 'msg': 'Bad request'}, 400)


class JudgeHandler(Resource):

    def get(self):
        return BAD_REQUEST


    def post(self):
        json_data = request.get_json()
        if json_data:
            try:
                requester = json_data['requester']
                sample_data = pd.DataFrame(data=json_data['data'],index=['x','y','z']).transpose()
                result = models[requester].predict(sample_data)
                response = {
                    'passed':result,
                    'requester':requester
                }
                print(requester)
                print(response)
                return response, 200
            except KeyError as e:
                return BAD_REQUEST
        else:
            return BAD_REQUEST


api.add_resource(JudgeHandler, '/judge')


@app.route('/')
def hello_world():
    return 'One Auth service'


if __name__ == '__main__':
    models = {}
    for root, dirs, files in os.walk('data'):
        for dir in dirs:
            models[dir]=One_auth()
            models[dir].fit(os.path.join(root,dir))
    app.run(host='0.0.0.0', port=5000)
