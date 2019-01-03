 precision mediump float;

 varying mediump vec2 aCoord;

 uniform sampler2D vTexture;
 uniform sampler2D vTexture2;

 void main()
 {
     vec3 texel = texture2D(vTexture, aCoord).rgb;
     texel = vec3(dot(vec3(0.3, 0.6, 0.1), texel));
     texel = vec3(texture2D(vTexture2, vec2(texel.r, .16666)).r);
     gl_FragColor = vec4(texel, 1.0);
 }