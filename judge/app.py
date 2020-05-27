from flask import Flask, request
from flask_restful import Api, Resource

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
                return sample_response, 200
            except KeyError as e:
                return BAD_REQUEST
        else:
            return BAD_REQUEST


api.add_resource(JudgeHandler, '/judge')


@app.route('/')
def hello_world():
    return 'One Auth service'


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
