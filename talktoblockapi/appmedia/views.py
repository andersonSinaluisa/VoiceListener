from django.shortcuts import render
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
import os


# Create your views here.
class SaveAudio(APIView):
    def post(self, request):
        audio = request.data.get('audio')
        if not os.path.exists('media/'):
            os.mkdir('media/')
        with open('media/' + str(audio), 'wb+') as destination:
            for chunk in audio.chunks():
                destination.write(chunk)
        file = 'media/' + str(audio)
        return Response({'mensaje':'guardado'},status = status.HTTP_200_OK)


class SaveText(APIView):
    def post(self, request):
        texto = request.data.get('texto')
        file = open("Mensaje.txt", "a")
        file.write(texto +"\n")
        file.close()
        return Response({'mensaje':'exitoso'},status=status.HTTP_200_OK)
