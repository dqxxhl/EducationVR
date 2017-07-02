#extension GL_OES_EGL_image_external : require

precision mediump float;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

void main() {
    vec4 color = texture2D(sTexture, vTextureCoord);
    gl_FragColor = color;
//    gl_FragColor = vec4(1.0,1.0,1.0,1.0);
}
