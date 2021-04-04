from django.urls import path
from .views import SaveAudio, SaveText
urlpatterns = [
    path('save-audio/', SaveAudio.as_view(),name='save_audio'),
    path('save-text/',SaveText.as_view(),name='save_text')
]
