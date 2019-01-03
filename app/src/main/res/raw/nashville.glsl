precision mediump float;

 varying mediump vec2 aCoord;

 uniform sampler2D vTexture;
 uniform sampler2D vTexture2;

 void main()
 {
     vec3 texel = texture2D(vTexture, aCoord).rgb;
     texel = vec3(
                  texture2D(vTexture2, vec2(texel.r, .16666)).r,
                  texture2D(vTexture2, vec2(texel.g, .5)).g,
                  texture2D(vTexture2, vec2(texel.b, .83333)).b);
     gl_FragColor = vec4(texel, 1.0);
 }
