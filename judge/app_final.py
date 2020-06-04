from flask import Flask, request
from flask_restful import Api, Resource
import pandas as pd
from one_class import One_auth
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
        print(json_data)
        if json_data:
            try:
                requester = json_data['requester']
                sample_data = pd.DataFrame(data=json_data['data'],index=['x','y','z']).transpose()
                result = model.predict(sample_data,requester)
                response = {
                    'passed':result,
                    'requester':requester
                }
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
    model = One_auth()
    model.fit()
    app.run(host='0.0.0.0', port=5000)