import requests
import json 
print("go")
download = False
session_handle = "1861964ab0d3e24190b0838375d8c5f9af274a2"
payload = [session_handle]
referer = "https://www.codingame.com/ide/1861964ab0d3e24190b0838375d8c5f9af274a2"
cookie = "rmcg=NUoqu1WPvsP1tjLQOhDgynK1FUJem3ElHnAY1yTmJnYYOODpoavCEKCo6Aswtipri/T9eJudAFxwa1L5dxl0HZDNa+mAkC+/RtGoFPOP7Ml0ZjJhGeLdiP8L+cgF9clC7OK43FPRfEwOsp0Yf4la6gjAVW4IRue07tWlnf72T3nbyKfGxB7nccHclyZUAAWmMtbd4lMVZTMbBhMqEnXykebTkKYKRoH92DX0msJ3/gpkoJATR/ZJoagH1Tfafe0mwhPT7P7ajVQwnnGT8xl/9NZaZbm0pMHpx2W7T+beNxzVJ/G9ghOjyg9sjCgI9QMSZPwOZFBo5XsxNFyAq7TCcttPkRnK9g4rZnUsonj+HJFZzktJBkoe9K3drno5i9Nc1tdxXeNnItC9aYjcO0He6tCYPLeruTfAH+O4l4gnMiMVt4GgXwPJwLQw3aMVivt5aWnCWBtSL0OaxyAO+pG37Q2sVdSAm+zujf4LKh/80KNuj4VFyqvP3YwmU+ldbtAJj/dLmzmipmuGTTOSdQjvbsZ8EvvhHURevnAoNVTuQELKGvS8UTzqDiPTCJFUgAM/Zgp9evFDMx+dBpEQH4PYWQ==; __utmx=47969717._RJFwe4FQie-Yl4bQU3sdQ$0:0.yQrWpW_QS92Z_lrQNJJ5Pw$0:3; __utmxx=47969717._RJFwe4FQie-Yl4bQU3sdQ$0:1444348756:8035200:.yQrWpW_QS92Z_lrQNJJ5Pw$0:1450395607:8035200; mp_undefined_mixpanel=%7B%22distinct_id%22%3A%201012859%2C%22%24initial_referrer%22%3A%20%22https%3A%2F%2Fwww.codingame.com%2Fide%2F1861964ab0d3e24190b0838375d8c5f9af274a2%22%2C%22%24initial_referring_domain%22%3A%20%22www.codingame.com%22%2C%22__mps%22%3A%20%7B%22%24os%22%3A%20%22Windows%22%2C%22%24browser%22%3A%20%22Chrome%22%2C%22%24browser_version%22%3A%2047%2C%22%24initial_referrer%22%3A%20%22https%3A%2F%2Fwww.codingame.com%2Fide%2F1861964ab0d3e24190b0838375d8c5f9af274a2%22%2C%22%24initial_referring_domain%22%3A%20%22www.codingame.com%22%2C%22%24email%22%3A%20%22kevinxdoran%40gmail.com%22%2C%22%24name%22%3A%20%22CloudLeaper%22%2C%22%24created%22%3A%20%222015-08-24T07%3A50%3A15%22%7D%2C%22__mpso%22%3A%20%7B%7D%2C%22__mpa%22%3A%20%7B%22Visit%20-%20Profile%22%3A%202%2C%22Visit%20-%20all%22%3A%202%7D%2C%22__mpu%22%3A%20%7B%7D%2C%22__mpap%22%3A%20%5B%5D%7D; JSESSIONID=71FF044C472E8291A3ECF7673F5149EE; AWSELB=49DF73731CB4F82B161CA4E04E72C058E44897F09EB695F93F530236FE10F4C2FFCD4A56B4190A3F6646A8BF34BA40FB2EFED581BC9F727F577483B0AE68F09E4357C9144E; _ga=GA1.2.899257171.1444348757; mp_367d5a11a4032fa993d1535dc63b0bc7_mixpanel=%7B%22distinct_id%22%3A%201012859%2C%22%24initial_referrer%22%3A%20%22%24direct%22%2C%22%24initial_referring_domain%22%3A%20%22%24direct%22%2C%22__mps%22%3A%20%7B%7D%2C%22__mpso%22%3A%20%7B%7D%2C%22__mpa%22%3A%20%7B%7D%2C%22__mpu%22%3A%20%7B%7D%2C%22__mpap%22%3A%20%5B%5D%7D; _gat=1"
headers = {'Content-type':'application/json', 
            'Accept':'application/json, text/plain',
            'Referer': referer,
            'Cookie': cookie}
r = requests.post("https://www.codingame.com/services/gamesPlayersRankingRemoteService/findAllByTestSessionHandle", 
                    data=json.dumps(payload),
                    headers=headers)
data = r.json()['success']
games = []
if download: 
    with open("out.json", 'w+') as output_file:
        for game in data[1:1000]:
            unknown_purpose = 1012859
            game_number = game['gameId']
            payload = [game_number, unknown_purpose]
            r = requests.post("https://www.codingame.com/services/gameResultRemoteService/findByGameId",
            data=json.dumps(payload),
            headers=headers)
            games.append(r.json())
        output_file.write(json.dumps(games))
else:
    with open("out.json", 'r') as input_file:
        games = json.loads(input_file.read())
        
with open("data.csv", 'w+') as data_file:
    for g in games:
        score_diff = []
        our_pos = 0;
        for user,points in zip(g['success']['agents'], g['success']['scores']):
            position = 3 - (points//10 - 7)
            user_info = user['codingamer']
            rank = user['rank']
            if 'pseudo' not in user_info:
                print(user_info)
            elif user_info['pseudo'] == 'CloudLeaper':
                our_pos = position
            else:
                score_diff.append((position, rank))
        for i in score_diff:
            data_file.write("" + str(i[1]) + ","  + str(i[0] - our_pos) + "\n");
                